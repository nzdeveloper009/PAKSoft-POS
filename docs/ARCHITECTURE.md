# App architecture

How the PBSF Android + iOS app is built. Read `README.md` first for context and `DOMAIN.md` for business rules; `API.md` is the server contract.

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
| `composeApp` | KMP + Compose | `App()`, session gate, `NavDisplay`, top-level navigation, `initKoin()`, iOS `MainViewController()` | every `feature:*`, `core:*` |
| `feature:<name>` | KMP + Compose | screens, ViewModels, UI state, Nav3 entries, Koin module | `core:domain`, `core:data`, `core:model`, `core:common`, `core:designsystem`, `core:ui` |
| `core:domain` | KMP | `CartCalculator`, `PricingRules`, use cases that combine repositories | `core:data`, `core:model`, `core:common` |
| `core:data` | KMP | repository interfaces + implementations, sync logic, DTO↔entity↔model mappers | `core:network`, `core:database`, `core:datastore`, `core:model`, `core:common` |
| `core:network` | KMP | Ktor client factory, API classes, `@Serializable` DTOs, `MoneySerializer`, error mapping | `core:model`, `core:common`, `core:datastore` (token + active shop) |
| `core:database` | KMP + Room | `PosDatabase`, entities, DAOs, platform builders | `core:model`, `core:common` |
| `core:datastore` | KMP | `AppPreferences` (DataStore), `TokenStore` (Keystore / Keychain) | `core:model`, `core:common` |
| `core:designsystem` | KMP + Compose | `PosTheme`, color/type/shape/spacing tokens, components, Nunito font, icons | — |
| `core:ui` | KMP + Compose | `UiText`, money/date formatters, state scaffolds, permission helpers, platform UI bridges (scanner, share) | `core:designsystem`, `core:model`, `core:common` |
| `core:model` | KMP (pure) | domain models: `Money`, `Shop`, `SellableItem`, `Cart`, `Sale`, enums | `core:common` |
| `core:common` | KMP (pure) | `AppResult`, `AppError`, `AppDispatchers`, `Clock`, id generation | — |
| `core:testing` | KMP | fakes, test dispatcher helpers, JSON fixture loader | anything it fakes |

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
- `Shop(id, displayName, apiBaseUrl, webBaseUrl)`; `Shops.all` is a hard-coded list (+ staging in debug builds).

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

- `core:network` converts every Ktor exception and error envelope into `AppError` in one function (`safeApiCall { }`). Repositories return `AppResult`; no exceptions cross a layer boundary. **Always rethrow `CancellationException`.**
- `Unauthenticated` is also published on a `SessionEvents` flow; `composeApp` listens and routes to login.

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
  - `HttpTimeout` (connect 15 s, request 30 s; uploads 120 s per request)
  - `DefaultRequest`: `Accept`, `Accept-Language`, `User-Agent`, `X-Device-Id`
  - Base URL resolved **per request** from the active shop (`ActiveShopProvider`), never baked into the client, so switching shop needs no new client
  - A small custom plugin adds `Authorization: Bearer <token>` read from `AuthTokenProvider` on every request (no token caching, so switching shop or logging out takes effect immediately); requests marked `SkipAuth` (login) get no header. A `401` → `AppError.Unauthenticated`; no refresh (Sanctum tokens don't refresh)
  - `Logging` only in debug builds, with `sanitizeHeader { it == HttpHeaders.Authorization }`; never log request bodies of `/auth/login`
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

- On iOS (non-JVM) the back stack is saved with kotlinx.serialization, so `navSavedStateConfig` must register **every** `NavKey` subclass in a polymorphic `SerializersModule`. Adding a route = adding it there; a missing one crashes on iOS only.
- Koin's `koin-compose-navigation3` integration is DSL-only today, so this app wires entries by hand as above and uses plain `koinViewModel()` inside each entry. This keeps the project in one DI style.
- **Top-level destinations** (bottom bar on phones, navigation rail on tablets), shown only when the user has the permission: Dashboard · POS · Sales · Ledger · More (Products, Customers, Cash register, Settings, later modules). This mirrors the web sidebar order.
- Session gate in `App()`: no active shop → Shop picker; no token → Login; token → bootstrap → main navigation.

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
    fun json(): Json = Json { ignoreUnknownKeys = true; explicitNulls = false; encodeDefaults = true }

    @Singleton
    fun httpClient(engine: HttpClientEngine, json: Json, tokens: TokenStore, shops: ActiveShopProvider): HttpClient =
        createPosHttpClient(engine, json, tokens, shops)
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

fun initKoin(platformConfig: KoinAppDeclaration? = null) =
    startKoin<PosKoinApp> {            // org.koin.plugin.module.dsl.startKoin
        platformConfig?.invoke(this)
    }

// androidApp — PosApplication.onCreate()
initKoin { androidContext(this@PosApplication) }

// iosApp — iOSApp.swift
init() { KoinInitKt.doInitKoin() }
```

- In Compose: `vm: SalesListViewModel = koinViewModel()` as a **default parameter** of the route composable, so previews and tests can pass a fake.
- Multi-module compile check: the entry point (`composeApp`) sees a module's definitions only through its compile classpath. `[KOIN-D001]` for a definition that exists → expose that module with `api(...)` or add it directly to `composeApp`.
- After setup, prove the check runs: remove a needed definition, build, expect a `[KOIN-…]` error, restore it.

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

Use window size classes (`org.jetbrains.compose.material3.adaptive`):

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

The app should feel like the web app: same purple, same POS button colors, same font. Source: `smf/public/css/custom-default.css` and `smf/resources/views/sale/pos.blade.php`.

| Token | Light | Use |
|---|---|---|
| `primary` | `#7C5CC4` | app bar actions, selected nav, primary buttons, POS button, active fields |
| `primaryDark` | `#6244A6` | pressed states, text on light purple |
| `primaryContainer` | `#E4E6FC` | selected rows, chips |
| `background` | `#FAFBFE` | screen background (web side nav and pages) |
| `surface` | `#FFFFFF` | cards, sheets |
| `success` | `#28A745` | Completed / Paid badges |
| `warning` | `#FFC107` | Draft / Partial badges |
| `danger` | `#DC3545` | Pending / Due badges, delete |
| `info` | `#17A2B8` | secondary actions (web "Brand" tab) |

POS payment button colors (exact web values): Card `#0984E3` · Cash `#00CEC9` · PayPal `#213170` · Draft `#E28D02` · Cheque `#FD7272` · Gift card `#5F27CD` · Deposit `#B33771` · Cancel `#D63031` · Recent transactions `#FFC107`. POS category tabs: Category `#7C5CC4`, Brand `#17A2B8`, Featured `#DC3545`.

- **Font:** Nunito (the web's font), bundled in `core:designsystem/src/commonMain/composeResources/font/`.
- Dark theme: derive from the same hues (primary lightened to about `#A48AE6`); not a straight inversion.
- Spacing scale 4/8/12/16/24/32 dp; corner radius 8 dp for cards, 6 dp for inputs, full for chips.
- Every color goes through `PosTheme`; no literal colors in feature code.
- Components to build first: `PosTopBar`, `PosButton`, `PaymentButton`, `StatusBadge`, `MoneyText`, `KpiTile` (dashboard), `SearchField` (with scan action), `ProductTile` (POS grid), `CartLineRow`, `EmptyState`, `ErrorBanner`, `LoadingState`.

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

```toml
[versions]
kotlin = "2.4.21"
agp = "9.5.1"
ksp = "2.3.13"                      # Room only; confirm Kotlin 2.4 support in KSP release notes
compose-multiplatform = "1.12.2"
compose-material3 = "1.9.0"         # org.jetbrains.compose.material3
compose-material3-adaptive = "1.3.0"
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
coil = "3.6.3"                      # coil3 + coil-network-ktor3
kermit = "2.2.0"
turbine = "1.2.1"
```
