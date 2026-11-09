# CLAUDE.md — working rules for the PBSF app

This folder is a **Compose Multiplatform (Android + iOS)** client for the SalePro POS of three shops: `smf`, `bsf` and `mummyandme` (Laravel 6). The app must behave **exactly like the website** for the same data. The web source lives next door at `../pbsf.org.pk/smf/` and is the reference for every behavior.

Out of scope: the WordPress site at the `pbsf.org.pk` root (Pakistan Blind Sports Federation) and all `wp-*` files. Never add features, content or branding from it.

## Read before you change anything

1. `README.md`: scope, roadmap, open decisions.
2. `docs/ARCHITECTURE.md`: modules, dependency rules, patterns, DI, navigation, design tokens, versions.
3. `docs/DOMAIN.md`: business rules with their legacy source files.
4. `docs/API.md`: the JSON contract (and Part 4 for server work).

If a task needs a rule these docs don't cover, read the legacy code (`../pbsf.org.pk/smf/...`), write the rule into `docs/DOMAIN.md` with its source file, then implement it.

## Golden rules

1. **Same behavior as the web.** Port behavior from the legacy controller or `pos.blade.php`; never invent business rules. Note the source in KDoc: `// Legacy: SaleController::store (stock deduction)`.
2. **Contract first.** Any request/response change goes into `docs/API.md` first, then the PHP side, then the Kotlin DTOs. DTO tests use the JSON examples from `API.md` as fixtures.
3. **Server owns stock and pricing.** The app's `CartCalculator` mirrors `DOMAIN.md` §5 for display; the server recomputes and can reject. Never move stock logic into the app.
4. **Money is `Money` (Long paisa).** Never `Double` or `Float` for money in models, entities, DTOs or UI state. JSON money is a 2-decimal string.
5. **Never write to a production shop** from code, tests or manual checks. Use the Staging shop or Ktor `MockEngine`. Reading production is fine.
6. **No secrets anywhere.** No tokens, passwords or `.env` values in code, logs, fixtures, docs or commits. Tokens live only in `TokenStore`.
7. **Follow the module rules** in `ARCHITECTURE.md` §2. Features never import other features. DTOs and entities never leave `core:data`.
8. **Koin: Annotations + Compiler Plugin only** (§DI below). No Koin KSP, no DSL modules, no `KoinComponent` in normal code.
9. **Common code first.** Platform code only behind `expect`/`actual` or an interface, in `androidMain`/`iosMain`. No `java.*`, `android.*` or `platform.*` imports in `commonMain`.
10. **Ask before** adding a dependency, a Gradle module, a new top-level destination, or anything listed under README → "Choices to confirm".

## Kotlin and Compose conventions

- Kotlin official code style. Prefer `internal` for implementations; `public` only for what other modules use.
- No `!!`. No `GlobalScope`. No `runBlocking` outside tests. Always rethrow `CancellationException`.
- Coroutines: inject `AppDispatchers`; never hard-code `Dispatchers.IO` outside `core:database` builder setup.
- Immutable UI state: `data class XxxUiState` with defaults; actions as `sealed interface XxxAction`; one-off effects via `Channel` → `Flow`.
- ViewModels: `@KoinViewModel`, expose `StateFlow` built with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`, a single `onAction(action)` entry.
- Composables: `XxxRoute` (gets the ViewModel via a `koinViewModel()` default parameter, handles effects and navigation callbacks) + `XxxScreen` (stateless) + at least one `@Preview` of `XxxScreen`.
- Collect with `collectAsStateWithLifecycle()`.
- All user-visible text in `composeResources/values/strings.xml` of the owning module; ViewModels emit `UiText`. Write apostrophes plainly (`isn't`): Compose resources don't unescape Android-style `\'`.
- Colors, type, shapes and spacing only from `PosTheme` (`core:designsystem`). No literal colors in features.
- Lists: `LazyColumn`/`LazyVerticalGrid` with stable `key`s.
- Every screen handles four states: loading, content, empty, error (cached content + error banner when a refresh fails).

## DI (Koin)

Use the `koin` skill for any DI work. Project rules:

- `@Singleton` for app-wide classes (never `@Single`), `@Factory` for short-lived ones, `@KoinViewModel` for ViewModels; imports from `org.koin.core.annotation.*`.
- Constructor injection only. Third-party objects (HttpClient, Room database, DataStore, Json) come from `@Singleton` functions in a `@Module`.
- Each Gradle module has exactly one root module in its `di` package with **all three**: `@Module @Configuration @ComponentScan("pk.paksoft.pos.<module package>")`. Missing one fails at runtime, not at compile time.
- Platform bindings: `@Module expect class PlatformXxxModule` in `commonMain`, `actual` in `androidMain`/`iosMain`, included from the common module.
- Koin starts once, in `initKoin()` (`composeApp`), via `startKoin<PosKoinApp>` from `org.koin.plugin.module.dsl`.
- A `[KOIN-…]` build error is a real graph problem: fix the definition; don't suppress it.

## Navigation

Navigation 3 (`ARCHITECTURE.md` §7). Each feature exposes `fun EntryProviderScope<NavKey>.xxxEntries(callbacks…)`; `composeApp` wires callbacks to the back stack. **Every new `NavKey` must be registered in the polymorphic `SerializersModule` in `composeApp`**, or iOS crashes on state restore. Use the `navigation-3` skill when changing navigation.

## Data and network

- Reads come from Room `Flow`s; `refresh()` fetches, maps and upserts in one transaction.
- Writes in Phase 1 go to the API; on success upsert the returned resource. Keep the same `client_uuid` across retries of one user action.
- Map Ktor failures and error envelopes to `AppError` only in `core:network` (`safeApiCall`).
- DTO properties always carry `@SerialName("snake_case")`. Money fields use `MoneySerializer`. Enums decode unknown values to `Unknown`.
- Room: money columns are `Long` paisa; reads return `Flow`; writes use `@Upsert`/`@Transaction`; schemas exported to `core/database/schemas`.

## Recipes

### Add a feature module (`feature:xyz`)

1. Add `include(":feature:xyz")` to `settings.gradle.kts`.
2. `feature/xyz/build.gradle.kts`: `plugins { id("pos.kmp.feature") }` and only extra dependencies.
3. Create, under `src/commonMain/kotlin/pk/paksoft/pos/feature/xyz/`:
   - `di/XyzModule.kt`: the Koin triad.
   - `navigation/XyzKeys.kt`: `@Serializable` keys implementing `NavKey`.
   - `navigation/XyzEntries.kt`: `EntryProviderScope<NavKey>.xyzEntries(...)`.
   - `<screen>/XyzViewModel.kt`, `XyzUiState.kt` (state + actions + effects), `XyzRoute.kt`, `XyzScreen.kt`.
4. `src/commonMain/composeResources/values/strings.xml`.
5. Tests in `src/commonTest/kotlin/...`: ViewModel tests with fakes and Turbine.
6. In `composeApp`: add the dependency, call `xyzEntries(...)`, register the keys in the `SerializersModule`, add the destination to top-level navigation if needed (permission-gated).

### Add an API call

1. Write or update the endpoint in `docs/API.md` (request, response, errors, permission).
2. Add the JSON example as a fixture in `core/testing` and a DTO decode/encode test.
3. DTO + API method in `core:network`; mapper + repository method in `core:data`; Room entity/DAO if cached.
4. Backend: implement per `API.md` Part 4 on **staging**, with a PHPUnit feature test.

### Port a web screen

1. Open the Blade view and its controller method; list every field, filter, button and permission check.
2. Check the sidebar condition in `resources/views/layout/main.blade.php` for who can see it.
3. Write down any rule not yet in `DOMAIN.md`.
4. Build the screen against fakes first (previews + ViewModel tests), then wire the repository.

## Testing (required)

| What | Where | How |
|---|---|---|
| `CartCalculator`, `PricingRules`, `Money` | `core:domain`, `core:model` `commonTest` | **Golden tests**: cases computed with the web's own JS formulas (`pos.blade.php`), covering exclusive and inclusive tax, per-unit discount, `*` and `/` units, customer-group markup, fixed and percentage coupons, minimum amount, shipping, order discount, rounding at each step |
| DTOs | `core:network` `commonTest` | decode every `API.md` example; encode requests and compare JSON |
| API classes | `core:network` | Ktor `MockEngine`: success, every error code in `API.md` §1.6, timeout, offline |
| Repositories | `core:data` | fake API + in-memory Room (where supported) or fake DAOs; refresh, upsert, failure keeps cache |
| ViewModels | each feature | fakes + Turbine; state for loading/content/empty/error and every action |
| DAOs | `core:database` | Android instrumented or host tests with an in-memory database |

Fakes over mocks. No network or real database in unit tests. Test names describe behavior: `` `inclusive tax splits net price and tax per unit`() ``.

## Before you say a task is done

Gradle needs JDK 17 (the default `java` on this Mac is newer), so every Gradle command carries the `JAVA_HOME` prefix.

```bash
# Tests in every module: iosSimulatorArm64Test + testAndroidHostTest
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests
# Tests in one module
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :<module>:iosSimulatorArm64Test
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :<module>:testAndroidHostTest

# Android app builds
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :androidApp:assembleDebug
# iOS framework links
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
# iOS app builds (expect "** BUILD SUCCEEDED **")
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro,OS=26.5' -derivedDataPath build/ios build | tail -3
```

iOS simulator tests run on "iPhone 17 Pro"; pass `-Ppos.iosTestDevice="<simulator name>"` to use another. The Xcode build runs Gradle in a build phase that doesn't inherit your shell, so it needs `local.properties` with `sdk.dir=<Android SDK path>` (gitignored) or `ANDROID_HOME` set for Xcode. Android host tests can't load Compose resources (`android.content.res.Resources` isn't mocked); a test that reads resource text goes in `iosTest`. The Xcode project is generated from `iosApp/project.yml`: after editing it, run `cd iosApp && xcodegen generate` and commit the regenerated project.

Report what you ran and its result. If something wasn't run (no simulator, no network), say so. For UI work, run the app and look at the screen; for POS math, include the golden test results.

## Backend (Laravel) work

- The legacy apps are production code on a shared host: **never edit files under `../pbsf.org.pk/` in place.** Work in a staging copy, as `API.md` Part 4 describes.
- Target **PHP 7.4 and Laravel 6.20**: no PHP 8 syntax (no `match`, named arguments, union types, `?->`, constructor promotion), no Laravel 8+ APIs.
- Put shared logic in `app/Services`; web and API controllers both call it. Don't copy logic into API controllers.
- Migrations must be guarded with `Schema::hasTable` / `Schema::hasColumn` (live databases were changed by hand).

## Repo housekeeping

- This folder is a git repository; its remote is https://github.com/nzdeveloper009/PAKSoft-POS (public). One commit per finished, building step; a commit is pushed only after its review passes. Never commit server details, credentials or customer data.
- Keep `docs/` current: a merged change that alters behavior, an endpoint or a module rule updates the matching doc in the same change.
