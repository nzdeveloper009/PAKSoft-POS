# App architecture

How the PAKSOFT POS Android + iOS app is built. Read `README.md` first for context and `DOMAIN.md` for business rules; `API.md` is the server contract.

Root package and Android `applicationId` / iOS bundle id: **`pk.paksoft.pos`** (confirmed by the owner on 9 November 2026; permanent after the first store release).

---

## 1. Big picture

```
┌──────────── Android app ────────────┐   ┌────────────── iOS app ──────────────┐
│ androidApp: Application, Activity   │   │ iosApp (Xcode): SwiftUI shell        │
└──────────────────┬──────────────────┘   └──────────────────┬──────────────────┘
                   └──────────────┬───────────────────────────┘
                          composeApp (shared root UI, Koin start, Nav3 host)
                                  │
            feature:auth · dashboard · pos · sales · products · customers · ledger · cashregister · settings
                                  │  (UI + ViewModels; features never depend on each other)
                       core:domain (CartCalculator, pricing rules, use cases)
                                  │
                       core:data (repositories: Room = source of truth, network refresh)
                     ┌────────────┼──────────────┐
             core:network   core:database   core:datastore
             (Ktor, DTOs)   (Room KMP)      (DataStore, TokenStore)
                     └────────────┼──────────────┘
                     core:model · core:common  (pure Kotlin, no platform code)
             core:designsystem · core:ui  (theme, components, formatters)
                                  │
                       HTTPS  →  <shop>/api/v1  (Laravel, see API.md)
```

## 2. Gradle modules

| Module | Kind | Contains | May depend on |
|---|---|---|---|
| `androidApp` | Android application | `PosApplication`, `MainActivity`, manifest, launcher icons | `composeApp` |
| `iosApp` | Xcode project | `iOSApp.swift`, `ContentView.swift` hosting `MainViewController()` | `composeApp` framework |
| `composeApp` | KMP + Compose (Kotlin package and Android namespace `pk.paksoft.pos.app`) | `App()`, session gate, `NavDisplay`, top-level navigation, `initKoin()`, iOS `MainViewController()` and `initKoinIos()` | every `feature:*`, `core:*` (all directly, so the Koin graph check sees them; `core:model` as `api` for `androidApp`) |
| `feature:<name>` | KMP + Compose | screens, ViewModels, UI state, Nav3 entries, Koin module | `core:domain`, `core:data`, `core:model`, `core:common`, `core:designsystem`, `core:ui` |
| `core:domain` | KMP | `CartCalculator`, `PricingRules`, use cases that combine repositories | `core:data`, `core:model`, `core:common` |
| `core:data` | KMP | repository interfaces + implementations, sync logic, DTO↔entity↔model mappers | `core:network`, `core:database`, `core:datastore`, `core:model`, `core:common` |
| `core:network` | KMP | Ktor client factory, API classes, `@Serializable` DTOs, `MoneySerializer`, error mapping | `core:model`, `core:common`, `core:datastore` (token + active shop) |
| `core:database` | KMP + Room | `PosDatabase`, entities, DAOs, platform builders | `core:model`, `core:common` |
| `core:datastore` | KMP | `AppPreferences` (DataStore), `TokenStore` (Keystore / Keychain) | `core:model`, `core:common` |
| `core:designsystem` | KMP + Compose | `PosTheme`, color/type/shape/spacing tokens, components, Nunito font, icons | — |
| `core:ui` | KMP + Compose | `UiText`, money/date formatters, state scaffolds, permission helpers, platform UI bridges (scanner, share) | `core:designsystem`, `core:model`, `core:common` |
| `core:model` | KMP (pure) | domain models: `Money`, `Shop`, `SellableItem`, `Cart`, `Sale`, enums | `core:common` |
| `core:common` | KMP (pure) | `AppResult`, `AppError`, `AppDispatchers`, `ApplicationScope`, id generation (`newUuid`) | — |
| `core:testing` | KMP | fakes (`FakeSessionRepository`, `FakeBootstrapRepository`, `FakeAuthApi`, `FakeBootstrapApi`, `FakeLocalDataCleaner`), `InMemoryAppPreferences`, `InMemoryTokenStore`, `TestData` (the `API.md` examples as models and DTOs). The raw JSON fixtures live in `core:network`'s `commonTest` (`ApiFixtures`) | anything it fakes |

Rules:

- **Features never depend on other features.** Cross-feature navigation goes through callbacks wired in `composeApp` (§7).
- `core:model` and `core:common` have no Compose, Ktor, Room or platform code.
- DTOs (`core:network`) and entities (`core:database`) never leave `core:data`; everything above it sees `core:model` types.
- New dependency direction not in this table → stop and update this file first.

## 3. Build setup

- **Version catalog:** `gradle/libs.versions.toml` (excerpt in §15).
- **Convention plugins** in `build-logic/convention`, applied by id:

| Plugin id | Applies / configures |
|---|---|
| `pos.kmp.library` | `org.jetbrains.kotlin.multiplatform` + `com.android.kotlin.multiplatform.library` (AGP 9 KMP library plugin; the Android target is configured as `kotlin { android { namespace = …; compileSdk = 37; minSdk = 26; androidResources { enable = true } } }`, not with `androidTarget()`; `androidResources` must be enabled for Compose resources), plus `iosArm64` and `iosSimulatorArm64`; JVM toolchain 17; namespace derived from the module path; compileSdk/minSdk; common test deps |
| `pos.kmp.compose` | `pos.kmp.library` + `org.jetbrains.compose` + `org.jetbrains.kotlin.plugin.compose`; Compose runtime/foundation/material3/resources |
| `pos.kmp.feature` | `pos.kmp.compose` + `pos.koin` + the standard feature dependencies from §2 + lifecycle ViewModel + Nav3 |
| `pos.koin` | `io.insert-koin.compiler.plugin` + Koin BOM, `koin-core`, `koin-annotations` (+ `koin-compose`, `koin-compose-viewmodel` for Compose modules). **No KSP for Koin.** |
| `pos.room` | `com.google.devtools.ksp` + `androidx.room` Gradle plugin; `room-runtime`, `sqlite-bundled`; `ksp<Target>` for every target; `room { schemaDirectory("$projectDir/schemas") }` |
| `pos.android.application` | `com.android.application` for `androidApp` (AGP 9 has built-in Kotlin; a KMP module can't also be an Android application, which is why `androidApp` is separate) |

- **Targets:** Android, `iosArm64` (devices), `iosSimulatorArm64` (Apple-silicon simulators). Intel simulators (`iosX64`) are not targeted.
- **Test tasks:** `iosSimulatorArm64Test` and `testAndroidHostTest` (Android host tests, enabled with `withHostTest`) per module; `allTests` runs both everywhere. `core:datastore` also has Android device tests (`connectedAndroidDeviceTest`). Android host tests can't load Compose resources, so tests that read resource text live in `iosTest`.
- **`composeApp` overrides** the path-derived namespace: Android namespace `pk.paksoft.pos.app`, Compose `Res` class in `pk.paksoft.pos.app.resources`, and an explicit framework `bundleId` (Koin's generated code sits outside the app package, so the compiler can't infer one).
- **SDK levels (my call, see README):** `minSdk 26`, `compileSdk 37`, `targetSdk 36`; iOS deployment target 16.0.
- **`gradle.properties`** must contain `android.disallowKotlinSourceSets=false` (KSP for Room with AGP 9 built-in Kotlin), `org.gradle.configuration-cache=true`, `org.gradle.caching=true`, `android.useAndroidX=true`, `android.nonTransitiveRClass=true`.
- **Gradle wrapper:** 9.7.1 (already cached on this machine).
- The Koin Compiler Plugin is applied through a convention plugin, so `build-logic`'s dependency on the Koin Gradle plugin must be `implementation`, not `compileOnly`.
- **KSP is used only by Room.** KSP versions are independent of Kotlin since 2.3.0; confirm the chosen KSP release supports the project's Kotlin version before bumping either.

## 4. Layers and patterns

### 4.1 Models (`core:model`)

- **`Money`**: `@JvmInline value class Money(val paisa: Long)`; arithmetic, `times(Double)` with **half-up rounding to 2 decimals** (to match the web's `toFixed(2)`, `DOMAIN.md` §5), `fromString("7998.00")`, `toPlainString()`. **Never `Double` for money** outside the calculator's internal steps.
- Quantities and rates are `Double` (the server stores doubles; quantities can be fractional with units like `/ 12`).
- Enums mirror `DOMAIN.md` §3 and always have an `Unknown` entry for forward compatibility.
- `Shop(id, displayName, apiBaseUrl, isDevelopment)`; `Shops.production` is a hard-coded list, and `Shops.forBuild(isDebug, localApiBaseUrl)` adds Demo, Staging and Local in debug builds.

### 4.2 Errors (`core:common`)

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Offline : AppError
    data object Timeout : AppError
    data object Unauthenticated : AppError                       // → logout
    data class Forbidden(val code: String, val message: String) : AppError
    data class NotFound(val message: String) : AppError
    data class Validation(val message: String, val fields: Map<String, List<String>>) : AppError
    data class Conflict(val message: String, val detailsJson: String?) : AppError
    data class Business(val code: String, val message: String, val detailsJson: String?) : AppError // insufficient_stock, totals_mismatch…
    data class Server(val status: Int, val message: String?) : AppError   // message is null for a non-JSON body (e.g. HTML 404)
    data class Unexpected(val cause: Throwable?) : AppError
}
```

- `core:network` converts every Ktor exception and error envelope into `AppError` in one place, `ApiCaller.call { }` / `callUnit { }`. Repositories return `AppResult`; no exceptions cross a layer boundary. **Always rethrow `CancellationException`.**
- Any `401` except `invalid_credentials` (including one whose body is not an error envelope) becomes `AppError.Unauthenticated` and is also published on `SessionEvents`. `DefaultSessionRepository` (`core:data`) listens, clears the token and cached data, and moves to `SessionState.LoggedOut(shop, SessionExpired)`; `composeApp`'s session gate only reacts to `SessionState`.

### 4.3 Repositories (`core:data`)

**Reads: Room is the source of truth.**

```kotlin
interface SalesRepository {
    fun observeSales(filter: SalesFilter): Flow<List<SaleSummary>>   // from Room
    suspend fun refreshSales(filter: SalesFilter): AppResult<Unit>    // network → upsert into Room
    suspend fun createSale(draft: SaleRequest): AppResult<CreatedSale> // network; on success upsert
}
```

- ViewModels collect the `Flow` and call `refresh…()` on open and on pull-to-refresh. A failed refresh shows an error banner over cached data instead of an empty screen.
- **Writes are online in Phase 1:** call the API, upsert the response into Room, return it. On `Offline`/`Timeout` the UI offers Retry with the **same `client_uuid`** (`API.md` §1.8).
- Implementations are `internal` classes annotated `@Singleton`; Koin auto-binds them to their interface.

### 4.4 Sync

- **Bootstrap** (`GET /bootstrap`): on login, app start and pull-to-refresh. Reference tables (warehouses, billers, customer groups, categories, brands, units, taxes, coupons, accounts) are **replaced** in one Room transaction.
- **Sellable items** (per selected warehouse) and **customers**: delta sync with `updated_since` (stored in a `sync_state` table keyed by resource + warehouse), plus a full refresh once a day and on pull-to-refresh (deletes can't be detected otherwise).
- **POS search is local** (Room `LIKE` on name/code) so typing and barcode scanning are instant; a lookup miss falls back to `GET /sellable-items/lookup`.
- **Phase 2 (planned): offline sales outbox.** A `pending_sale` table holds the request JSON + `client_uuid`; a sync worker (Android WorkManager, iOS foreground flush on resume) posts them in order. Idempotency prevents duplicates; `insufficient_stock`/`totals_mismatch` failures go to a "Needs attention" list. Once this table exists, Room migrations must be real (no destructive fallback) because the table holds unsynced money.

### 4.5 Domain (`core:domain`)

- **`CartCalculator`**: a pure function from `(lines, orderTaxRate, orderDiscount, shippingCost, coupon)` to every line and order figure in `DOMAIN.md` §5, rounding at the same steps as the web. This is the most important unit-tested class in the app (golden tests, see `CLAUDE.md`).
- **`PricingRules`**: `DOMAIN.md` §4 steps 5–7 (customer-group markup, sale-unit conversion, manual override) on top of the server's `base_price`.
- Use cases only when they combine several repositories or hold real logic (`CompleteSaleUseCase`, `SwitchShopUseCase`). Don't wrap a single repository call in a use case.

## 5. Networking (`core:network`)

- Engines: **OkHttp** on Android, **Darwin** on iOS (platform Koin modules, §8).
- One `HttpClient`, configured with:
  - `ContentNegotiation` + `Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }`
  - `HttpTimeout` (connect 15 s, request 30 s, socket 30 s; uploads 120 s per request when they arrive) and `expectSuccess = false` (`ApiCaller` maps the status)
  - Base URL resolved **per request** from the active shop: `ApiEndpoint.url(path)` asks `ActiveShopProvider`, so nothing is baked into the client and switching shop needs no new client
  - One custom plugin, `PosHeaders`, sets `Accept`, `Accept-Language`, `User-Agent`, `X-Device-Id` and `Authorization: Bearer <token>` on every request, reading the token from `AuthTokenProvider` each time (no caching, so switching shop or logging out takes effect immediately); requests carrying the `SkipAuth` attribute (login) get no `Authorization` header. A `401` → `AppError.Unauthenticated`; no refresh (Sanctum tokens don't refresh)
  - Debug builds wrap the engine in `DemoAwareEngine` (`core:network/demo`): requests to the reserved host `demo.paksoft.invalid` (the Demo shop) are answered in-app by `DemoServer` with invented data (`auth/login`, `auth/logout`, `me`, `bootstrap`; anything else is a `not_found` envelope); every other request goes to the real engine. Release builds never wrap it.
  - `Logging` only in debug builds, at `LogLevel.INFO` (URL, method and status; never bodies) with `sanitizeHeader { it == HttpHeaders.Authorization }`
- DTOs: `@Serializable` data classes with `@SerialName("snake_case")` on every property; money fields use `MoneySerializer` (string ↔ `Money`).
- One API class per area (`AuthApi`, `BootstrapApi`, `CatalogApi`, `CustomerApi`, `SalesApi`, `CashRegisterApi`, `LedgerApi`), each a thin `@Singleton` wrapper returning `AppResult<Dto>`.

## 6. Persistence

### 6.1 Room (`core:database`)

```kotlin
@Database(entities = [/* … */], version = 1, exportSchema = true)
@ConstructedBy(PosDatabaseConstructor::class)
abstract class PosDatabase : RoomDatabase() {
    abstract fun sellableItemDao(): SellableItemDao
    // …
}

@Suppress("KotlinNoActualForExpect")
expect object PosDatabaseConstructor : RoomDatabaseConstructor<PosDatabase> {
    override fun initialize(): PosDatabase
}

fun buildPosDatabase(builder: RoomDatabase.Builder<PosDatabase>): PosDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
```

- Platform builders: Android `Room.databaseBuilder<PosDatabase>(context, context.getDatabasePath("pos.db").absolutePath)`; iOS `Room.databaseBuilder<PosDatabase>(name = "<NSDocumentDirectory>/pos.db")`.
- DAOs expose `Flow` for reads and `suspend` upserts (`@Upsert`) and `@Transaction` replace-all functions.
- Money columns store **paisa as `Long`**.
- Phase 1 may use destructive migration (it's a cache). From the first table that holds unsynced data (outbox), migrations are mandatory and tested.
- **One database, one shop at a time:** logout or shop switch calls `clearAllTables()` (blocked while an outbox has pending rows).
- A clear starts a new `SessionEpoch` (`core:data`) under its lock; a `refresh()` notes the epoch before its network call and writes only if it is unchanged, so an answer that lands after logout or shop switch is dropped.

### 6.2 DataStore and secrets (`core:datastore`)

- `AppPreferences` over `DataStore<Preferences>` created with `PreferenceDataStoreFactory.createWithPath { "<platform dir>/pos.preferences_pb".toPath() }`. Holds: active shop id, last warehouse/biller per user, POS options, theme, device id, last sync times.
- **The auth token is not stored in plain DataStore.** `TokenStore` is an interface with platform implementations: Android encrypts with an Android Keystore key before writing to its own DataStore file; iOS uses the Keychain (`kSecClassGenericPassword`, `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`).

## 7. Navigation (Navigation 3 for Compose Multiplatform)

- Naming: a destination's **key** is `…Key` (a `@Serializable` object/class implementing `NavKey`); its wiring composable is `…Route`; its stateless UI is `…Screen`. Keys live in the feature that owns the screen (`feature/sales/.../navigation/SalesKeys.kt`).
- Each feature exposes an entry builder and takes callbacks for anything outside itself:

```kotlin
// feature:sales
@Serializable data object SalesListKey : NavKey
@Serializable data class SaleDetailKey(val saleId: Long) : NavKey

fun EntryProviderScope<NavKey>.salesEntries(
    onOpenSale: (Long) -> Unit,
    onOpenReceipt: (Long) -> Unit,
) {
    entry<SalesListKey> { SalesListRoute(onOpenSale = onOpenSale) }
    entry<SaleDetailKey> { key -> SaleDetailRoute(saleId = key.saleId, onOpenReceipt = onOpenReceipt) }
}
```

- `composeApp` owns the back stack and wires the callbacks:

```kotlin
val backStack = rememberNavBackStack(navSavedStateConfig, DashboardKey)
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),      // ViewModel per entry, so koinViewModel() is per screen
    ),
    entryProvider = entryProvider {
        dashboardEntries(onOpenSale = { backStack.add(SaleDetailKey(it)) })
        salesEntries(onOpenSale = { backStack.add(SaleDetailKey(it)) }, onOpenReceipt = { backStack.add(ReceiptKey(it)) })
        // …
    },
)
```

- On iOS (non-JVM) the back stack is saved with kotlinx.serialization, so `navSavedStateConfig` (`composeApp/.../navigation/NavSavedState.kt`) must register **every** `NavKey` subclass in a polymorphic `SerializersModule`. Adding a route = adding it there; a missing one crashes on iOS. `NavSavedStateTest` (`composeApp` `iosTest`) round-trips every top-level key.
- Koin's `koin-compose-navigation3` integration is DSL-only today, so this app wires entries by hand as above and uses plain `koinViewModel()` inside each entry. This keeps the project in one DI style.
- **Top-level destinations** (bottom bar on phones, navigation rail on tablets), shown only when the user has the permission: Dashboard · POS · Sales · Ledger · More (Products, Customers, Cash register, Settings, later modules). This mirrors the web sidebar order. `TopLevelDestination` (`composeApp`) holds each tab's key, label, icon, required permissions and whether it needs the shop's ledger feature; `visibleDestinations(permissions, ledgerEnabled)` filters them. Phase 0: POS needs `sales-add`, Sales needs `sales-index`, Ledger needs `sales-index` and `features.ledger`.
- `MainScaffold` keeps **one back stack** with Dashboard as the fixed start destination: selecting Dashboard sets it to `[DashboardKey]`, any other tab to `[DashboardKey, thatTabKey]` (`backStackFor`). So Back from a tab returns to Dashboard, and Back on Dashboard leaves the app. The highlighted tab is the one owning the last top-level key on the stack (`selectedDestination`), so screens pushed on top of a tab keep it selected; if that tab is no longer visible, the stack falls back to Dashboard. Below 600 dp of width it shows a Material 3 `NavigationBar`, otherwise a `NavigationRail` (plain `BoxWithConstraints`; no adaptive library yet).
- Session gate in `App()`, driven only by `SessionRepository.state`: `Starting` → loading; `NoShop` → shop picker; `LoggedOut` → login (keyed by shop id); `LoggedIn` → `MainScaffold`. A saved session shows at once, offline too; `GET /me` refreshes it in the background. `AppViewModel` lives at the root (activity / iOS view controller) and calls `SessionRepository.start()` once.
- `MainViewModel` also lives at the root, so it outlives logins: it refreshes bootstrap every time the state becomes `LoggedIn` (app start with a saved session, or a new login), cancels that refresh when the session ends, and shows a refresh failure (except `Unauthenticated`) as a banner with Retry above the tab content.
- The app draws edge to edge: `MainScaffold` handles the system-bar insets for the tabs, and `App()` pads the shop picker and login (which have no app bar) with `safeDrawingPadding()`.
- **Transitions** (spec "Motion moments"; every spec comes from `PosTransitions` in `core:designsystem`, built from `PosMotion`, so reduced motion applies):
  - Session gate: `App()` puts an `AnimatedContent` inside the app's one `SharedTransitionLayout`, keyed by `sessionScreen(state)` and the login's shop id (a refreshed session or notice doesn't animate). `sessionTransition(from, to)` (`SessionTransitions.kt`) picks shared axis forward (picker → login), shared axis back (log out, change shop) or fade through (leaving start-up, arriving home). The gate provides `LocalSharedTransitionScope` and its own scope as `LocalPosAnimatedVisibilityScope`, so the shop picker and login share elements with `posSharedElement`/`posSharedBounds` and `shopCardKey`/`shopAvatarKey`/`shopNameKey`.
  - Home: `MainScaffold`'s `NavDisplay` slides pushes and pops on the shared X axis (`transitionSpec`, `popTransitionSpec`). Tab entries (`isTopLevelKey`) carry `NavDisplay.TransitionKey`/`PopTransitionKey` metadata for fade through, because switching tabs replaces the stack and `NavDisplay` would otherwise slide it as a push or pop. `predictivePopTransitionSpec` is `PosTransitions.predictiveBack`: on the linear `gesture` timeline the leaving screen shrinks to 0.9 as the user swipes, and an entry decorator rounds its corners to 28 dp with a light shadow (`Modifier.predictiveBackCorners`); a back swipe from a tab shrinks too. The same decorator gives each entry `LocalNavAnimatedContentScope` as `LocalPosAnimatedVisibilityScope`.
  - `NavDisplay`'s scene state and back handler are hoisted (`rememberSceneState`, `rememberNavigationEventState`, `NavigationBackHandler`) only so the decorator knows when a predictive gesture runs; the gesture flag is handed down as a `State` and read in the entries, and the scene strategies and decorator list are remembered, so a gesture never rebuilds the scenes mid-transition.
  - Predictive back needs `android:enableOnBackInvokedCallback="true"` in the manifest (set). It runs where `NavDisplay` gets predictive back events (Android 14+); elsewhere the normal pop runs.
  - The bar and rail sit on `surfaceContainer` with a `secondaryContainer` indicator, `PosIcons` and `labelMedium`; a newly selected icon bumps (`Modifier.selectionBump`).

## 8. Dependency injection (Koin Annotations + Koin Compiler Plugin)

Style: **Koin Annotations**, compiled and graph-checked at build time by the **Koin Compiler Plugin**. No Koin KSP, no DSL modules, no `checkModules()` tests.

- Bindings: `@Singleton` (not `@Single`) for app-wide objects, `@Factory` for short-lived ones, `@KoinViewModel` for ViewModels. Imports from `org.koin.core.annotation.*`.
- Constructor injection everywhere. `KoinComponent` only in the iOS Swift bridge, if one is ever needed.
- Each Gradle module has one root module in its `di` package with the **triad**:

```kotlin
package pk.paksoft.pos.feature.sales.di

@Module
@Configuration
@ComponentScan("pk.paksoft.pos.feature.sales")
class SalesModule
```

  Missing any of the three → runtime `NoDefinitionFoundException`, not a compile error.

- Third-party objects come from **function builders** in a module:

```kotlin
@Module(includes = [PlatformNetworkModule::class])
@Configuration
@ComponentScan("pk.paksoft.pos.core.network")
class NetworkModule {
    @Singleton
    fun json(): Json = PosJson   // ignoreUnknownKeys, explicitNulls = false, encodeDefaults

    @Singleton
    fun httpClient(
        engine: HttpClientEngine,
        json: Json,
        tokens: AuthTokenProvider,
        prefs: AppPreferences,
        @Provided config: AppConfig,
    ): HttpClient = createPosHttpClient(engine, json, tokens, prefs, config)
}

// commonMain
@Module expect class PlatformNetworkModule
// androidMain
@Module actual class PlatformNetworkModule { @Singleton fun engine(): HttpClientEngine = OkHttp.create() }
// iosMain
@Module actual class PlatformNetworkModule { @Singleton fun engine(): HttpClientEngine = Darwin.create() }
```

- App start (one shared function, `composeApp/commonMain`):

```kotlin
@KoinApplication
object PosKoinApp

fun initKoin(config: AppConfig, platform: KoinAppDeclaration? = null) {
    startKoin<PosKoinApp> {            // org.koin.plugin.module.dsl.startKoin
        koin.declare(config)           // consumers take `@Provided config: AppConfig`
        platform?.invoke(this)
    }
}

// androidApp — PosApplication.onCreate() (androidApp depends on koin-android for androidContext)
initKoin(AppConfig(BuildConfig.DEBUG, "Android", Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, "http://10.0.2.2:8010/api/v1")) {
    androidContext(this@PosApplication)
}

// composeApp iosMain — KoinInitIos.kt: initKoinIos() builds the iOS AppConfig (Platform.isDebugBinary, UIDevice, bundle version)
// iosApp — iOSApp.swift
init() { KoinInitIosKt.doInitKoinIos() }
```

- `AppConfig` (debug flag, platform, OS and app version, Local API URL) is not a definition: it's declared at start and injected with `@Provided`.

- In Compose: `vm: SalesListViewModel = koinViewModel()` as a **default parameter** of the route composable, so previews and tests can pass a fake.
- Multi-module compile check: the entry point (`composeApp`) sees a module's definitions only through its compile classpath. `[KOIN-D001]` for a definition that exists → expose that module with `api(...)` or add it directly to `composeApp`. That's why `composeApp` depends on every `core:*` module directly.
- The check is proven to run at `PosKoinApp` (Phase 0): an unbound constructor parameter fails the build with `[KOIN-D001]` on Android and iOS, and dropping `core:datastore` from `composeApp` fails the Android build with `[KOIN-D001] Missing dependency: …TokenStore`. Repeat the probe after changing how Koin starts.

## 9. UI architecture (per screen)

Unidirectional data flow:

```kotlin
data class SalesListUiState(
    val isRefreshing: Boolean = false,
    val sales: List<SaleSummary> = emptyList(),
    val filter: SalesFilter = SalesFilter.Default,
    val banner: UiText? = null,
)

sealed interface SalesListAction {
    data object Refresh : SalesListAction
    data class FilterChanged(val filter: SalesFilter) : SalesListAction
    data class SaleClicked(val id: Long) : SalesListAction
}

@KoinViewModel
class SalesListViewModel(private val sales: SalesRepository) : ViewModel() {
    val state: StateFlow<SalesListUiState> = /* combine(repository flow, local state) */
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesListUiState())
    private val _effects = Channel<SalesListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()
    fun onAction(action: SalesListAction) { /* … */ }
}

@Composable
fun SalesListRoute(onOpenSale: (Long) -> Unit, vm: SalesListViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    ObserveEffects(vm.effects) { if (it is SalesListEffect.OpenSale) onOpenSale(it.id) }
    SalesListScreen(state = state, onAction = vm::onAction)
}

@Composable
fun SalesListScreen(state: SalesListUiState, onAction: (SalesListAction) -> Unit) { /* stateless */ }
```

- `…Route` = wiring (ViewModel, effects, navigation callbacks). `…Screen` = stateless and previewable.
- One-off events (navigate, show snackbar) go through a `Channel` → `Flow` of effects, never through state flags you have to reset.
- Strings live in Compose resources (`composeResources/values/strings.xml`); ViewModels expose `UiText`, not raw strings.

## 10. Adaptive layout

Phase 0 needs only the navigation switch, done with a 600 dp width breakpoint (`BoxWithConstraints` in `MainScaffold`). The two-pane screens below will use window size classes (`org.jetbrains.compose.material3.adaptive`, not in the catalog yet; adding it needs the owner's OK):

| Screen | Compact (phone) | Medium/Expanded (tablet, landscape) |
|---|---|---|
| POS | Tabs: **Products** (grid + search + scan) and **Cart** (badge with item count); pay sheet slides up | Two panes like the web: cart and totals on the left, product grid on the right; payment buttons along the bottom |
| Sales, Products, Customers | List → detail screen | List and detail side by side |
| Ledger editor | Horizontally scrollable grid, frozen first column and header row | Same grid, more columns visible |
| Navigation | Bottom bar | Navigation rail |

## 11. Platform features (expect/actual or interfaces in `core:ui`)

| Feature | Android | iOS | Phase |
|---|---|---|---|
| Camera barcode scan | CameraX + ML Kit barcode scanning | AVFoundation `AVCaptureMetadataOutput` (UIKit view in Compose) | 1 |
| Hardware scanner (USB/Bluetooth keyboard wedge) | search field submits on Enter | same | 1 |
| Share receipt as PDF | `PdfDocument` + share intent | `UIGraphicsPDFRenderer` + share sheet | 1 |
| Thermal printer (ESC/POS over Bluetooth) | to be designed | to be designed | 2 |
| Ledger import file picker | Storage Access Framework | `UIDocumentPicker` | 1 |

## 12. Design system (`core:designsystem`)

Phase 1 look (spec: `docs/superpowers/specs/2026-11-10-phase1-look-and-motion-design.md`): ledger green on warm paper, Material 3 Expressive. `PosTheme(darkTheme)` wraps `MaterialExpressiveTheme` with our light/dark `ColorScheme`s, `MotionScheme.expressive()` (with reduced motion, a scheme built from `PosMotion`'s snaps and 120 ms fades, so M3 components follow the setting too), `PosShapes` and `posTypography()`. `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` appears only in `core:designsystem`; features call our wrappers.

| Token | Where | Notes |
|---|---|---|
| Scheme colours | `MaterialTheme.colorScheme` (`PosLightColorScheme`, `PosDarkColorScheme`) | primary `#0B6B4F` light / `#7EDAB3` dark; errors use `error*`, success is primary. Every text pair is AA, checked by `ContrastTest` |
| Extended colours | `PosTheme.colors` (`PosExtendedColors`) | `due`, `dueContainer`/`onDueContainer`, `paidContainer`/`onPaidContainer`, `chartIn`, `chartOut` |
| Type | `MaterialTheme.typography` + `PosTheme.typography` (`moneyLarge/Medium/Small`, tabular figures) | Nunito variable, weights 400-900 |
| Shape | `MaterialTheme.shapes` (`PosShapes`: 8/12/16/24/28 dp) | |
| Motion | `PosTheme.motion` (`PosMotion`) | spring tokens `fastSpatial`, `spatial`, `slowSpatial`, `fastEffects`, `effects`, `slowEffects`, `bouncy`, `countUp`; `shimmer()` loop; `gesture()` (linear `gestureMillis` tween for transitions a gesture seeks, such as predictive back); `staggerStepMillis`, `enterOffset`, `rollingStaggerMillis`. Reads the OS reduce-motion setting through `rememberReduceMotion()` (`expect`/`actual`): spatial specs snap, effects become a 120 ms tween |
| Shop identity | `ShopBrand.forShop(id, displayName)` and `ShopBrand.colors()` | initials, `ShopShape` (`polygon()` gives the `MaterialShapes` polygon) and light/dark colour pairs for `smf`, `bsf`, `mummyandme`, `demo`, other. Shown on the shop card, login header and signed-in header only; buttons, charts and money stay green |

- **Font:** Nunito (the web's font), bundled in `core:designsystem/src/commonMain/composeResources/font/`.
- Spacing scale 4/8/12/16/24/32 dp.
- Every colour, duration, easing and spring goes through `PosTheme`; no literals in feature code.
- Built (`component/`, `icon/`): `PosButton(style = Filled | Charge | Tonal | Outlined | Text, large, loading, icon)` (shape morph and squeeze on press; `loading` narrows it into a circle with `PosLoadingIndicator`), `PosFilterChip`, `ShopAvatar(brand, size)`, `RollingText` (odometer digits), `animateCountAsState`, `SkeletonBox` / `Modifier.skeleton(visible)`, `PosTextField`, `PosTopBar`, `EmptyState(title, message, icon)`, `ErrorBanner`, `LoadingState`, `Modifier.selectionBump(selected)` (nav icons), and `PosIcons` (the only icon source for features).
- Press feel for custom tappable surfaces: `Modifier.posClickable(pressedScale, shape) { }.background(color)` (ripple + `pressScale`; the shape is clipped inside the scale so shape, background and ripple shrink together); `Modifier.pressScale(interactionSource)` before the clip for components that own an interaction source. Entrances: `Modifier.enterStagger(index)` for a screen's sections, not lazy-list rows (previews show sections in place). Under reduced motion the squeeze, the entrance, the roll and the count are skipped and the final state shows at once.
- Transitions (`motion/`): `PosTransitions.sharedAxisForward(motion, density)` / `sharedAxisBack(motion, density)` (40 dp slide + fade; incoming `spatial`, outgoing `fastEffects`), `fadeThrough(motion)` (outgoing fades with `fastEffects`, incoming fades and scales 0.94 → 1 with `spatial`) and `predictiveBack(motion, density)`; none sets a size transform. Shared elements only through `Modifier.posSharedElement(key)` (same content travels) and `Modifier.posSharedBounds(key)` (a container grows into another), which read `LocalSharedTransitionScope` and `LocalPosAnimatedVisibilityScope` and do nothing when either is missing (previews) or motion is reduced (`sharedTransitionEnabled`). Shop keys: `shopCardKey(id)`, `shopAvatarKey(id)`, `shopNameKey(id)`. `Modifier.predictiveBackCorners(scope, inGesture)` rounds a screen pulled by predictive back.
- Components still to build: `StatusBadge`, `MoneyText`, `KpiTile` (dashboard), `SearchField` (with scan action), `ProductTile` (POS grid), `CartLineRow`.

## 13. Formatting

- Money: shop currency `code` + `position` from bootstrap (`prefix` → `PKR 7,998.00`), grouping `#,##0.00` like the web.
- Dates: shop `date_format` (PHP format such as `d/m/Y`) converted once to a kotlinx-datetime format; times in the shop's timezone (`Asia/Karachi`).

## 14. Logging, crashes, security

- Logging with Kermit; debug-only verbose logs. Never log tokens, passwords, full request bodies of auth calls, or customer phone numbers.
- Crash reporting is a later decision (Crashlytics or Sentry); keep a `CrashReporter` interface so it plugs in without touching features.
- No secrets in the repo: no API keys exist in the app at all (only per-user tokens). `local.properties` and signing files stay out of version control.
- Release builds: R8 enabled on Android with keep rules for kotlinx.serialization and Ktor.

## 15. Version catalog excerpt

Latest stable releases on Maven Central / Google Maven, checked 9 November 2026. Bump deliberately, one family at a time.

AGP is pinned to 9.4.0, the newest version the team's Android Studio supports; do not bump it past what the IDE supports.

```toml
[versions]
kotlin = "2.4.21"
agp = "9.4.0"
ksp = "2.3.13"                      # Room only; confirm Kotlin 2.4 support in KSP release notes
compose-multiplatform = "1.12.2"
compose-material3 = "1.12.0-alpha03"  # org.jetbrains.compose.material3
compose-material3-adaptive = "1.3.0"  # planned; not in the catalog yet
compose-material-icons = "1.7.3"    # org.jetbrains.compose.material:material-icons-core
androidx-activity-compose = "1.13.0"
jb-lifecycle = "2.11.0"             # org.jetbrains.androidx.lifecycle (viewmodel-compose, runtime-compose, viewmodel-navigation3)
jb-navigation3 = "1.2.0"            # org.jetbrains.androidx.navigation3:navigation3-ui
koin-bom = "4.2.2"
koin-compiler = "1.2.3"             # io.insert-koin.compiler.plugin
ktor = "3.6.1"
room = "2.8.5"
sqlite = "2.7.1"                    # androidx.sqlite:sqlite-bundled; keep in step with Room
datastore = "1.3.0"
kotlinx-coroutines = "1.11.0"
kotlinx-serialization = "1.12.0"
kotlinx-datetime = "0.8.0"
coil = "3.6.3"                      # coil3 + coil-network-ktor3; planned, not in the catalog yet
kermit = "2.2.0"
turbine = "1.2.1"
```
