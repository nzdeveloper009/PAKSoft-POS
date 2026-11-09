# Phase 0 App Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An Android + iOS Compose Multiplatform app where a user picks one of the three shops (SMF, BSF, Mummy & Me, plus dev-only Local and Staging), logs in with their web username or email, lands on a bottom-navigation home (Dashboard, POS, Sales, Ledger, More; placeholders except More), stays logged in across restarts and offline, and can refresh shop data, switch shop and log out.

**Architecture:** Multi-module KMP project with Gradle convention plugins. `core:*` modules (common, model, designsystem, ui, datastore, network, database, data, testing) and two features (`auth`, `settings`) behind a shared `composeApp` that starts Koin and hosts Navigation 3. Room caches the bootstrap data, DataStore holds preferences, the token sits in the Keystore/Keychain, and Ktor talks to `<shop>/api/v1`.

**Tech Stack:** Kotlin 2.4.21, Compose Multiplatform 1.12.2 (Material 3 1.9.0), AGP 9.5.1, Gradle 9.7.1, Koin 4.2.2 + Koin Compiler Plugin 1.2.3 (Annotations), Ktor 3.6.1, Room 2.8.5 + KSP 2.3.13, DataStore 1.3.0, Navigation 3 (JetBrains) 1.2.0, kotlinx.serialization 1.12.0, kotlinx-datetime 0.8.0, Kermit 2.2.0, Turbine 1.2.1.

**Spec:** `PBSF/README.md`, `PBSF/CLAUDE.md`, `PBSF/docs/ARCHITECTURE.md`, `PBSF/docs/API.md` (Part 1, A1–A4), `PBSF/docs/DOMAIN.md` §1–2.

Paths: `$APP` = `/Users/syed/AndroidStudioProjects/PAKSOFTSolutionsPvtLtd/public_html/PBSF`. Reference project for working AGP 9 + KMP settings on this Mac: `/Users/syed/AndroidStudioProjects/KmpProjects/RSMSmartAttendance` (read-only).

Every Gradle command runs as `cd $APP && JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew …`.

## Global Constraints

- Root package `pk.paksoft.pos`; Android `applicationId` and iOS bundle id `pk.paksoft.pos`; app display name **PAKSOFT POS**. Module packages: `pk.paksoft.pos.core.<name>`, `pk.paksoft.pos.feature.<name>`, `pk.paksoft.pos.app` (composeApp), `pk.paksoft.pos.android` (androidApp).
- Nothing from the Pakistan Blind Sports Federation WordPress site (no name, branding or content). "PBSF" appears only as the folder name.
- Targets: Android (`minSdk 26`, `compileSdk 37`, `targetSdk 36`), `iosArm64`, `iosSimulatorArm64`; iOS deployment target 16.0; JVM toolchain 17.
- Koin: Annotations + Compiler Plugin only (`@Singleton`, `@Factory`, `@KoinViewModel`, `@Module @Configuration @ComponentScan("<module package>")` per Gradle module, in a `di` package). No `koin-ksp-compiler`, no `com.google.devtools.ksp` for Koin, no DSL modules, no `KoinComponent`, no `checkModules()`. KSP only for Room.
- Money is `Money(paisa: Long)`; never `Double`/`Float` for money.
- DTOs (`core:network`) and Room entities (`core:database`) never appear outside `core:data` (and `core:testing`).
- No literal colors outside `core:designsystem`. User-visible text only from Compose resources, except server-provided error `message`s which are shown as given.
- Never send write requests to a production shop. The only shop used for manual testing is **Local (dev)**.
- Never print, log or commit tokens or passwords. Ktor logging (debug only) at `LogLevel.INFO` with `Authorization` sanitized; request bodies never logged.
- Local git in `$APP`; one commit per task, only when that task's verification passes. A commit step that gives only a message means `git add -A && git commit -m "<message>"`.
- Shops (exact):

| id | displayName | apiBaseUrl | build |
|---|---|---|---|
| `smf` | Super Madina Solar Fan | `https://pbsf.org.pk/smf/api/v1` | all |
| `bsf` | BSF | `https://pbsf.org.pk/bsf/api/v1` | all |
| `mummyandme` | Mummy & Me | `https://pos.mummyandme.com.pk/api/v1` | all |
| `staging` | Staging (dev) | `https://pbsf.org.pk/smf-staging/api/v1` | debug only |
| `local` | Local API (dev) | Android `http://10.0.2.2:8010/api/v1`, iOS `http://127.0.0.1:8010/api/v1` | debug only |

- Exact UI copy:

| Key (module) | Text |
|---|---|
| `shop_picker_title` (auth) | Choose your shop |
| `shop_picker_subtitle` (auth) | Pick the business you want to work in. |
| `shop_dev_badge` (auth) | Dev |
| `login_title` (auth) | Log in |
| `login_shop_label` (auth) | Shop: %1$s |
| `login_field_login` (auth) | Username or email |
| `login_field_password` (auth) | Password |
| `login_show_password` / `login_hide_password` (auth) | Show / Hide |
| `login_button` (auth) | Log in |
| `login_change_shop` (auth) | Change shop |
| `login_error_login_required` (auth) | Enter your username or email. |
| `login_error_password_required` (auth) | Enter your password. |
| `error_offline` (ui) | No internet connection. Check your connection and try again. |
| `error_timeout` (ui) | The server took too long to answer. Please try again. |
| `error_shop_not_connected` (ui) | This shop isn't connected to the app yet. |
| `error_server` (ui) | Something went wrong on the server. Please try again. |
| `error_unexpected` (ui) | Something went wrong. Please try again. |
| `session_expired` (ui) | Your session ended. Please log in again. |
| `more_title` (settings) | More |
| `more_signed_in_as` (settings) | Signed in as %1$s (%2$s) |
| `more_shop` (settings) | Shop: %1$s |
| `more_refresh` (settings) | Refresh shop data |
| `more_refreshed` (settings) | Shop data updated. |
| `more_switch_shop` (settings) | Switch shop |
| `more_logout` (settings) | Log out |
| `nav_dashboard`, `nav_pos`, `nav_sales`, `nav_ledger`, `nav_more` (composeApp) | Dashboard, POS, Sales, Ledger, More |
| `coming_soon` (composeApp) | Coming in the next update. |

## Review Focus

1. A shop whose API isn't deployed yet (all three production shops today) answers with an HTML 404 page; login must show "This shop isn't connected to the app yet." instead of crashing. Tests: `ApiCallerTest.html 404 becomes Server 404 without message` (Task 5), `AppErrorUiTextTest.server 404 without message means shop not connected` (Task 3), `LoginViewModelTest.html 404 shows shop not connected banner` (Task 9).
2. Opening the app offline with a saved session must keep the user signed in on cached data. Test: `DefaultSessionRepositoryTest.start offline with cached session stays logged in` (Task 8).
3. A token revoked on the server (any `401 unauthenticated`) must return the user to login with "Your session ended. Please log in again." and clear the token and cached data. Tests: `DefaultSessionRepositoryTest.unauthenticated event expires the session` (Task 8), `LoginViewModelTest.expired notice shows banner` (Task 9).
4. Switching shop must never send the previous shop's token or show its cached data. Tests: `DefaultSessionRepositoryTest.switch shop clears token and local data` (Task 8), `PosHttpClientTest.token is read per request for the active shop` (Task 5).
5. Double-tapping Log in must send one request. Test: `LoginViewModelTest.second submit while submitting is ignored` (Task 9).

---

### Task 1: Project skeleton that builds on Android and iOS

**Files:**
- Create: `$APP/.gitignore`, `$APP/settings.gradle.kts`, `$APP/build.gradle.kts`, `$APP/gradle.properties`, `$APP/gradle/libs.versions.toml`, `$APP/gradlew`, `$APP/gradlew.bat`, `$APP/gradle/wrapper/gradle-wrapper.jar`, `$APP/gradle/wrapper/gradle-wrapper.properties`
- Create: `$APP/build-logic/settings.gradle.kts`, `$APP/build-logic/convention/build.gradle.kts`, `$APP/build-logic/convention/src/main/kotlin/{KmpLibraryConventionPlugin,KmpComposeConventionPlugin,KoinConventionPlugin,KmpFeatureConventionPlugin,RoomConventionPlugin,AndroidApplicationConventionPlugin}.kt`, `$APP/build-logic/convention/src/main/kotlin/pk/paksoft/pos/buildlogic/ProjectExtensions.kt`
- Create: `$APP/composeApp/build.gradle.kts`, `composeApp/src/commonMain/kotlin/pk/paksoft/pos/app/App.kt`, `composeApp/src/iosMain/kotlin/pk/paksoft/pos/app/MainViewController.kt`
- Create: `$APP/androidApp/build.gradle.kts`, `androidApp/src/main/AndroidManifest.xml`, `androidApp/src/main/kotlin/pk/paksoft/pos/android/MainActivity.kt`, launcher icon resources
- Create: `$APP/iosApp/project.yml`, `iosApp/iosApp/{iOSApp.swift,ContentView.swift,Info.plist}`, `iosApp/iosApp/Assets.xcassets`, generated `iosApp/iosApp.xcodeproj`
- Modify: `$APP/CLAUDE.md` ("Before you say a task is done" commands)

**Interfaces:**
- Produces: convention plugin ids `pos.kmp.library`, `pos.kmp.compose`, `pos.koin`, `pos.kmp.feature`, `pos.room`, `pos.android.application`; version catalog aliases used by every later task; `@Composable fun App()` in `composeApp`; iOS `fun MainViewController(): UIViewController`; framework name `ComposeApp`.

- [ ] **Step 1: Git and wrapper**

`cd $APP && git init -b main`. `.gitignore`: `.gradle/`, `build/`, `*/build/`, `.idea/`, `.kotlin/`, `local.properties`, `*.iml`, `.DS_Store`, `iosApp/iosApp.xcodeproj/xcuserdata/`, `iosApp/iosApp.xcodeproj/project.xcworkspace/xcuserdata/`, `*.jks`, `*.keystore`. Copy `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` from the reference project; write `gradle-wrapper.properties` with `distributionUrl=https\://services.gradle.org/distributions/gradle-9.7.1-bin.zip`.

- [ ] **Step 2: Version catalog and Gradle settings**

`libs.versions.toml` holds the versions in Tech Stack plus `compose-material-icons = "1.7.3"`, `androidx-activity = "1.13.0"`, `android-compileSdk = "37"`, `android-targetSdk = "36"`, `android-minSdk = "26"`, and libraries: Compose (`org.jetbrains.compose.runtime:runtime`, `…foundation:foundation`, `…ui:ui`, `…ui:ui-tooling-preview`, `…ui:ui-tooling`, `…components:components-resources`, `org.jetbrains.compose.material3:material3`, `org.jetbrains.compose.material:material-icons-core`), `androidx.activity:activity-compose`, JetBrains lifecycle (`lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-navigation3`), `org.jetbrains.androidx.navigation3:navigation3-ui`, Koin BOM + `koin-core`, `koin-annotations`, `koin-compose`, `koin-compose-viewmodel`, `koin-android`, `koin-test`, Ktor (`client-core`, `client-content-negotiation`, `serialization-kotlinx-json`, `client-logging`, `client-okhttp`, `client-darwin`, `client-mock`), Room (`room-runtime`, `room-compiler`), `androidx.sqlite:sqlite-bundled`, `androidx.datastore:datastore-preferences-core`, kotlinx (`coroutines-core`, `coroutines-test`, `serialization-json`, `datetime`), `co.touchlab:kermit`, `app.cash.turbine:turbine`, `org.jetbrains.kotlin:kotlin-test`, and Gradle plugin artifacts for build-logic (AGP, Kotlin, Compose, Compose compiler, Kotlin serialization, KSP, Room Gradle plugin, `io.insert-koin:koin-compiler-gradle-plugin`). `[plugins]` aliases for every plugin id, all applied `apply false` in the root `build.gradle.kts`.
`settings.gradle.kts`: `pluginManagement { includeBuild("build-logic"); repositories { google(); mavenCentral(); gradlePluginPortal() } }`, `dependencyResolutionManagement` with `google()` + `mavenCentral()`, `rootProject.name = "PaksoftPos"`, `include(":composeApp", ":androidApp")`.
`gradle.properties`: as in `ARCHITECTURE.md` §3 plus `kotlin.code.style=official`, `org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8`, `kotlin.daemon.jvmargs=-Xmx3072M`.

- [ ] **Step 3: Convention plugins**

`build-logic/convention` uses `kotlin-dsl` and depends (`implementation`, not `compileOnly`) on every plugin artifact above. Register the six ids. Behavior:
- `pos.kmp.library`: applies `org.jetbrains.kotlin.multiplatform` + `com.android.kotlin.multiplatform.library`; `kotlin { jvmToolchain(17); android { namespace = "pk.paksoft.pos." + <project path without leading ':' and ':' → '.'> ; compileSdk = 37; minSdk = 26; withHostTest { } }; iosArm64(); iosSimulatorArm64(); compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") } }`; `commonTest` gets `kotlin-test`, `kotlinx-coroutines-test`, `turbine`; every `KotlinNativeSimulatorTest` task uses `device = providers.gradleProperty("pos.iosTestDevice").orElse("iPhone 17 Pro")`. If `withHostTest {}` isn't the AGP 9.5 name, find the host-test opt-in in the AGP KMP DSL and use it.
- `pos.kmp.compose`: `pos.kmp.library` + `org.jetbrains.compose` + `org.jetbrains.kotlin.plugin.compose`; `android { androidResources { enable = true } }`; `commonMain` gets runtime, foundation, ui, material3, material-icons-core, components-resources, ui-tooling-preview; `compose.resources { packageOfResClass = "<namespace>.resources"; publicResClass = true }` (features show strings from `core:ui`, so its `Res` must be public).
- `pos.koin`: `io.insert-koin.compiler.plugin`; `commonMain` gets `platform(koin-bom)`, `koin-core`, `koin-annotations`; when `org.jetbrains.compose` is applied, also `koin-compose`, `koin-compose-viewmodel`.
- `pos.kmp.feature`: `pos.kmp.compose` + `pos.koin` + `org.jetbrains.kotlin.plugin.serialization`; `commonMain` gets `:core:common`, `:core:model`, `:core:data`, `:core:designsystem`, `:core:ui`, lifecycle viewmodel-compose + runtime-compose, navigation3-ui; `commonTest` gets `:core:testing`.
- `pos.room`: `com.google.devtools.ksp` + `androidx.room`; `room { schemaDirectory("$projectDir/schemas") }`; `commonMain` gets `room-runtime`, `sqlite-bundled`; `dependencies { add("kspAndroid", room-compiler); add("kspIosArm64", …); add("kspIosSimulatorArm64", …) }`.
- `pos.android.application`: `com.android.application` + `org.jetbrains.kotlin.plugin.compose`; `compileSdk 37`, `minSdk 26`, `targetSdk 36`, Java 17, `buildFeatures { buildConfig = true; compose = true }`.

- [ ] **Step 4: Minimal shared app and platform shells**

`composeApp/build.gradle.kts`: plugins `pos.kmp.compose`; iOS targets produce a static framework `baseName = "ComposeApp"`. `App()` shows a `Text("PAKSOFT POS")` inside `MaterialTheme`. `MainViewController() = ComposeUIViewController { App() }`.
`androidApp`: plugin `pos.android.application`, `namespace = "pk.paksoft.pos.android"`, `applicationId = "pk.paksoft.pos"`, `versionCode = 1`, `versionName = "0.1.0"`, depends on `:composeApp`, `activity-compose`, `ui-tooling` (debug). `MainActivity` calls `enableEdgeToEdge()` and `setContent { App() }`. Manifest label `PAKSOFT POS`, `INTERNET` permission. Adaptive launcher icon: background `#7C5CC4`, white foreground vector of a simple till/receipt shape.
`iosApp/project.yml` (XcodeGen): target `iosApp`, `PRODUCT_BUNDLE_IDENTIFIER: pk.paksoft.pos`, `PRODUCT_NAME: PAKSOFT POS` with `CFBundleDisplayName` `PAKSOFT POS`, deployment target 16.0, `FRAMEWORK_SEARCH_PATHS: "$(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)"`, `OTHER_LDFLAGS: ["$(inherited)", "-framework", "ComposeApp"]`, `ENABLE_USER_SCRIPT_SANDBOXING: NO`, and a pre-build script `cd "$SRCROOT/.." && export JAVA_HOME="$(/usr/libexec/java_home -v 17)" && ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`. `Info.plist` includes `NSAppTransportSecurity` → `NSAllowsLocalNetworking = true`, `CADisableMinimumFrameDurationOnPhone = true`. `ContentView` wraps `MainViewControllerKt.MainViewController()` in a `UIViewControllerRepresentable` with `.ignoresSafeArea()`.
Run: `brew install xcodegen && cd $APP/iosApp && xcodegen generate`.

- [ ] **Step 5: Build both platforms**

Run: `./gradlew :androidApp:assembleDebug` → Expected: `BUILD SUCCESSFUL`, `androidApp/build/outputs/apk/debug/androidApp-debug.apk` exists.
Run: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` → `BUILD SUCCESSFUL`.
Run: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 17 Pro' -derivedDataPath build/ios build | tail -3` → `** BUILD SUCCEEDED **`.

- [ ] **Step 6: Record verified test task names**

Run `./gradlew :composeApp:tasks --group verification`; note the iOS simulator test task (expected `iosSimulatorArm64Test`) and the Android host test task (expected `testAndroidHostTest`). Rewrite CLAUDE.md "Before you say a task is done" with the exact commands this project uses, including the `JAVA_HOME` prefix and the `xcodebuild` line above.

- [ ] **Step 7: Commit**

`git add -A && git commit -m "build: KMP project skeleton with convention plugins, Android and iOS shells"` (also commits the existing `README.md`, `CLAUDE.md`, `docs/`).

---

### Task 2: `core:common` and `core:model`

**Files:**
- Create: `core/common/build.gradle.kts` (`pos.kmp.library`, `pos.koin`), `core/common/src/commonMain/kotlin/pk/paksoft/pos/core/common/{AppResult.kt,AppError.kt,AppDispatchers.kt,ApplicationScope.kt,Ids.kt,di/CommonModule.kt}`
- Create: `core/model/build.gradle.kts` (`pos.kmp.library`; depends on `:core:common`, `kotlinx-datetime`), `core/model/src/commonMain/kotlin/pk/paksoft/pos/core/model/{Money.kt,Shop.kt,AppConfig.kt,Session.kt,Permissions.kt,ShopData.kt}`
- Test: `core/model/src/commonTest/kotlin/pk/paksoft/pos/core/model/{MoneyTest.kt,ShopsTest.kt,UserSessionTest.kt}`
- Modify: `settings.gradle.kts` (include both), `docs/ARCHITECTURE.md` §4.2 (replace the `AppError` block with the one below)

**Interfaces:**
- Produces (`pk.paksoft.pos.core.common`):
  - `sealed interface AppResult<out T> { data class Success<T>(val value: T); data class Failure(val error: AppError) }` plus `inline fun <T, R> AppResult<T>.map(f: (T) -> R): AppResult<R>`, `onSuccess`, `onFailure`, `fun <T> AppResult<T>.getOrNull(): T?`.
  - `sealed interface AppError { data object Offline; data object Timeout; data object Unauthenticated; data class Forbidden(val code: String, val message: String); data class NotFound(val message: String); data class Validation(val message: String, val fields: Map<String, List<String>>); data class Conflict(val message: String, val detailsJson: String?); data class Business(val code: String, val message: String, val detailsJson: String?); data class Server(val status: Int, val message: String?); data class Unexpected(val cause: Throwable?) }`
  - `data class AppDispatchers(val io: CoroutineDispatcher, val default: CoroutineDispatcher, val main: CoroutineDispatcher)`; `CommonModule` provides `@Singleton fun dispatchers() = AppDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main)`.
  - `@Singleton class ApplicationScope(dispatchers: AppDispatchers) : CoroutineScope` (SupervisorJob + default dispatcher).
  - `fun newUuid(): String` (kotlin.uuid, lowercase string).
- Produces (`pk.paksoft.pos.core.model`):
  - `@JvmInline value class Money(val paisa: Long) : Comparable<Money>`: `operator fun plus/minus`, `fun toPlainString(): String` (`"7998.00"`, `"-0.50"`), `companion object { val ZERO; fun parse(text: String): Money }` (accepts `-?\d+(\.\d{1,2})?`; otherwise `IllegalArgumentException`).
  - `data class Shop(val id: String, val displayName: String, val apiBaseUrl: String, val isDevelopment: Boolean)`; `object Shops { val production: List<Shop>; fun forBuild(isDebug: Boolean, localApiBaseUrl: String): List<Shop> }` (table in Global Constraints; dev shops appended after production).
  - `data class AppConfig(val isDebug: Boolean, val platformName: String, val osVersion: String, val appVersion: String, val localApiBaseUrl: String)`.
  - `data class AppUser(val id: Long, val name: String, val email: String, val phone: String?, val companyName: String?, val roleId: Int, val roleName: String, val warehouseId: Long?, val billerId: Long?)`; `enum class StaffScope { All, Own, Unknown }`; `data class UserSession(val user: AppUser, val permissions: Set<String>, val staffScope: StaffScope) { fun can(permission: String): Boolean }`.
  - `object Permissions { const val SALES_INDEX = "sales-index"; const val SALES_ADD = "sales-add" }`.
  - `sealed interface SessionState { data object Starting; data object NoShop; data class LoggedOut(val shop: Shop, val notice: SessionNotice?); data class LoggedIn(val shop: Shop, val session: UserSession) }`; `enum class SessionNotice { SessionExpired }`.
  - `ShopData.kt`: `ShopSettings(title: String, logoUrl: String?, developedBy: String?, dateFormat: String, invoiceFormat: String?, gstState: Int?, timezone: String, currency: Currency?, features: ShopFeatures, pos: PosDefaults)`, `Currency(id: Long, code: String, name: String, exchangeRate: Double, position: CurrencyPosition)`, `enum class CurrencyPosition { Prefix, Suffix, Unknown }`, `ShopFeatures(ledger: Boolean, previousBalance: Boolean)`, `PosDefaults(customerId: Long?, warehouseId: Long?, billerId: Long?, pageSize: Int?, keyboardActive: Boolean)`.

- [ ] **Step 1: Write the failing tests**

```kotlin
class MoneyTest {
    @Test fun `parses two decimal strings`() { assertEquals(Money(799800), Money.parse("7998.00")) }
    @Test fun `parses whole and one decimal`() { assertEquals(Money(799800), Money.parse("7998")); assertEquals(Money(550), Money.parse("5.5")) }
    @Test fun `parses negatives`() { assertEquals(Money(-50), Money.parse("-0.50")) }
    @Test fun `rejects junk`() { listOf("", "1,000.00", "1.234", "abc", "1.").forEach { assertFailsWith<IllegalArgumentException> { Money.parse(it) } } }
    @Test fun `formats plain`() { assertEquals("7998.00", Money(799800).toPlainString()); assertEquals("-0.50", Money(-50).toPlainString()); assertEquals("0.00", Money.ZERO.toPlainString()) }
    @Test fun `adds and subtracts`() { assertEquals(Money(150), Money(100) + Money(50)); assertEquals(Money(50), Money(100) - Money(50)) }
}

class ShopsTest {
    @Test fun `release build has exactly the three shops`() { assertEquals(listOf("smf", "bsf", "mummyandme"), Shops.forBuild(false, "http://x").map { it.id }) }
    @Test fun `debug build adds staging and local after them`() {
        val shops = Shops.forBuild(true, "http://10.0.2.2:8010/api/v1")
        assertEquals(listOf("smf", "bsf", "mummyandme", "staging", "local"), shops.map { it.id })
        assertEquals("http://10.0.2.2:8010/api/v1", shops.last().apiBaseUrl)
        assertTrue(shops.last().isDevelopment)
    }
    @Test fun `smf url is exact`() { assertEquals("https://pbsf.org.pk/smf/api/v1", Shops.production.first { it.id == "smf" }.apiBaseUrl) }
}

class UserSessionTest {
    @Test fun `can checks permission names`() { /* session with setOf("sales-index") → can("sales-index") true, can("sales-add") false */ }
}
```

- [ ] **Step 2: Run them and see them fail**

Run: `./gradlew :core:model:iosSimulatorArm64Test` → Expected: compilation FAIL (types missing).

- [ ] **Step 3: Implement the types listed under Interfaces**

- [ ] **Step 4: Run the tests**

Run: `./gradlew :core:model:iosSimulatorArm64Test :core:model:testAndroidHostTest :core:common:assemble` → Expected: PASS.

- [ ] **Step 5: Commit**

`feat(core): common result/error types and core models`

---

### Task 3: `core:designsystem` and `core:ui`

**Files:**
- Create: `core/designsystem/build.gradle.kts` (`pos.kmp.compose`), `core/designsystem/src/commonMain/kotlin/pk/paksoft/pos/core/designsystem/{theme/PosColors.kt,theme/PosTypography.kt,theme/PosSpacing.kt,theme/PosTheme.kt,component/PosButton.kt,component/PosTextField.kt,component/PosTopBar.kt,component/ErrorBanner.kt,component/LoadingState.kt,component/EmptyState.kt}`, `core/designsystem/src/commonMain/composeResources/font/nunito_variable.ttf`
- Create: `core/ui/build.gradle.kts` (`pos.kmp.compose`; depends on `:core:common`, `:core:model`, `:core:designsystem`, lifecycle runtime-compose), `core/ui/src/commonMain/kotlin/pk/paksoft/pos/core/ui/{UiText.kt,ObserveEffects.kt,AppErrorUiText.kt}`, `core/ui/src/commonMain/composeResources/values/strings.xml`
- Test: `core/designsystem/src/commonTest/kotlin/.../PosColorsTest.kt`, `core/ui/src/commonTest/kotlin/.../AppErrorUiTextTest.kt`

**Interfaces:**
- Produces (`…core.designsystem`): `@Composable fun PosTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)`; `object PosTheme { val colors: PosExtendedColors @Composable get }`; `data class PosExtendedColors(success, warning, danger, info, payCard, payCash, payPaypal, payDraft, payCheque, payGiftCard, payDeposit, payCancel, payRecent: Color)`; `val PosLightColorScheme`, `val PosDarkColorScheme`; `object PosSpacing { xs = 4.dp; s = 8.dp; m = 12.dp; l = 16.dp; xl = 24.dp; xxl = 32.dp }`; components `PosButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false)`, `PosOutlinedButton(text, onClick, modifier, enabled)`, `PosTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, error: String? = null, isPassword: Boolean = false, passwordVisible: Boolean = false, trailing: (@Composable () -> Unit)? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, keyboardActions: KeyboardActions = KeyboardActions.Default, enabled: Boolean = true)`, `PosTopBar(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {})`, `ErrorBanner(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null)`, `LoadingState(modifier: Modifier = Modifier)`, `EmptyState(title: String, message: String, modifier: Modifier = Modifier)`.
- Produces (`…core.ui`): `sealed interface UiText { data class Raw(val text: String); data class Res(val res: StringResource, val args: List<Any> = emptyList()) }`, `@Composable fun UiText.asString(): String`; `@Composable fun <T> ObserveEffects(effects: Flow<T>, onEffect: suspend (T) -> Unit)` (collects while STARTED); `fun AppError.toUiText(): UiText` mapping: Offline → `error_offline`; Timeout → `error_timeout`; Unauthenticated → `session_expired`; Forbidden/NotFound/Validation/Conflict/Business → `Raw(message)`; `Server(404, null)` → `error_shop_not_connected`; `Server(_, message != null)` → `Raw(message)`; `Server(_, null)` → `error_server`; Unexpected → `error_unexpected`.

- [ ] **Step 1: Get permission for the font**

Ask the user: download `Nunito[wght].ttf` (variable, about 270 KB, SIL Open Font License) from `https://github.com/google/fonts/raw/main/ofl/nunito/Nunito%5Bwght%5D.ttf` into `core/designsystem/src/commonMain/composeResources/font/nunito_variable.ttf`. If they decline, use `FontFamily.Default` and note it in `ARCHITECTURE.md` §12.

- [ ] **Step 2: Write the failing tests**

```kotlin
class PosColorsTest {
    @Test fun `brand primary matches the web`() { assertEquals(Color(0xFF7C5CC4), PosLightColorScheme.primary) }
    @Test fun `payment colors match pos blade`() {
        val c = PosLightExtendedColors
        assertEquals(Color(0xFF0984E3), c.payCard); assertEquals(Color(0xFF00CEC9), c.payCash); assertEquals(Color(0xFF213170), c.payPaypal)
        assertEquals(Color(0xFFE28D02), c.payDraft); assertEquals(Color(0xFFFD7272), c.payCheque); assertEquals(Color(0xFF5F27CD), c.payGiftCard)
        assertEquals(Color(0xFFB33771), c.payDeposit); assertEquals(Color(0xFFD63031), c.payCancel); assertEquals(Color(0xFFFFC107), c.payRecent)
    }
}

class AppErrorUiTextTest {
    @Test fun `server 404 without message means shop not connected`() { assertEquals(UiText.Res(Res.string.error_shop_not_connected), AppError.Server(404, null).toUiText()) }
    @Test fun `server message is shown as given`() { assertEquals(UiText.Raw("Down"), AppError.Server(503, "Down").toUiText()) }
    @Test fun `business errors show the server message`() { assertEquals(UiText.Raw("The username/email or password is incorrect."), AppError.Business("invalid_credentials", "The username/email or password is incorrect.", null).toUiText()) }
    @Test fun `offline timeout unauthenticated unexpected map to their strings`() { /* Offline→error_offline, Timeout→error_timeout, Unauthenticated→session_expired, Unexpected(null)→error_unexpected */ }
}
```

- [ ] **Step 3: Run them and see them fail**

Run: `./gradlew :core:designsystem:iosSimulatorArm64Test :core:ui:iosSimulatorArm64Test` → FAIL (missing).

- [ ] **Step 4: Implement**

Colors from `ARCHITECTURE.md` §12 (light: primary `#7C5CC4`, onPrimary white, primaryContainer `#E4E6FC`, onPrimaryContainer `#6244A6`, background `#FAFBFE`, surface `#FFFFFF`, error `#DC3545`; dark: primary `#A48AE6`, background `#14121C`, surface `#1C1928`, primaryContainer `#2A2342`). Typography: Material 3 type scale with `FontFamily(Font(Res.font.nunito_variable, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))), …600, …700)`. `PosButton` shows a small `CircularProgressIndicator` instead of the text while `loading`, and is disabled while loading. `PosTextField` is an `OutlinedTextField` with `supportingText` = `error` and `isError = error != null`; `isPassword` uses `PasswordVisualTransformation` unless `passwordVisible`. Add one `@Preview` per component.

- [ ] **Step 5: Run the tests**

Run: same as Step 3 plus `:core:designsystem:testAndroidHostTest :core:ui:testAndroidHostTest` → PASS.

- [ ] **Step 6: Commit**

`feat(core): design system tokens, components and UI text helpers`

---

### Task 4: `core:datastore`

**Files:**
- Create: `core/datastore/build.gradle.kts` (`pos.kmp.library`, `pos.koin`; deps `:core:common`, `datastore-preferences-core`, `kotlinx-coroutines-core`), `core/datastore/src/commonMain/kotlin/pk/paksoft/pos/core/datastore/{AppPreferences.kt,DataStoreAppPreferences.kt,TokenStore.kt,di/DataStoreModule.kt,di/PlatformDataStoreModule.kt}`, `src/androidMain/kotlin/…/{KeystoreTokenStore.kt,di/PlatformDataStoreModule.android.kt}`, `src/iosMain/kotlin/…/{KeychainTokenStore.kt,di/PlatformDataStoreModule.ios.kt}`
- Test: `core/datastore/src/iosTest/kotlin/pk/paksoft/pos/core/datastore/{DataStoreAppPreferencesTest.kt,KeychainTokenStoreTest.kt}`

**Interfaces:**
- Produces:
  - `interface AppPreferences { val activeShopId: Flow<String?>; suspend fun setActiveShopId(id: String?); suspend fun deviceId(): String; val cachedSessionJson: Flow<String?>; suspend fun setCachedSessionJson(json: String?) }`. `deviceId()` creates a UUID (`newUuid()`) on first call and returns the same value afterwards.
  - `@Singleton class DataStoreAppPreferences(private val dataStore: DataStore<Preferences>) : AppPreferences` (keys `active_shop_id`, `device_id`, `cached_session_json`).
  - `interface TokenStore { suspend fun read(shopId: String): String?; suspend fun write(shopId: String, token: String); suspend fun clear(shopId: String) }`.
  - Android `KeystoreTokenStore(context: Context)`: AES/GCM key `pos_token_key` in `AndroidKeyStore`; stores `base64(iv):base64(ciphertext)` under key `token_<shopId>` in its own DataStore file `pos_tokens.preferences_pb`.
  - iOS `KeychainTokenStore(service: String = "pk.paksoft.pos.token")`: `kSecClassGenericPassword`, account = shopId, `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`.
  - `@Module expect class PlatformDataStoreModule` providing `DataStore<Preferences>` (file `pos.preferences_pb` in Android `filesDir` / iOS `NSDocumentDirectory`) and `TokenStore`; `DataStoreModule` = triad + `includes = [PlatformDataStoreModule::class]`.

- [ ] **Step 1: Write the failing tests (iOS simulator)**

```kotlin
class DataStoreAppPreferencesTest {
    private fun prefs() = DataStoreAppPreferences(PreferenceDataStoreFactory.createWithPath { (NSTemporaryDirectory() + "prefs-${newUuid()}.preferences_pb").toPath() })
    @Test fun `active shop round trip`() = runTest { val p = prefs(); assertNull(p.activeShopId.first()); p.setActiveShopId("smf"); assertEquals("smf", p.activeShopId.first()); p.setActiveShopId(null); assertNull(p.activeShopId.first()) }
    @Test fun `device id is stable`() = runTest { val p = prefs(); val a = p.deviceId(); assertEquals(a, p.deviceId()); assertEquals(36, a.length) }
    @Test fun `cached session round trip`() = runTest { val p = prefs(); p.setCachedSessionJson("{}"); assertEquals("{}", p.cachedSessionJson.first()) }
}

class KeychainTokenStoreTest {
    private val store = KeychainTokenStore(service = "pk.paksoft.pos.token.test")
    @Test fun `write read clear per shop`() = runTest {
        store.write("smf", "t1"); store.write("bsf", "t2")
        assertEquals("t1", store.read("smf")); assertEquals("t2", store.read("bsf"))
        store.clear("smf"); assertNull(store.read("smf")); assertEquals("t2", store.read("bsf")); store.clear("bsf")
    }
    @Test fun `write replaces existing token`() = runTest { store.write("smf", "a"); store.write("smf", "b"); assertEquals("b", store.read("smf")); store.clear("smf") }
}
```

(Keychain on the simulator test runner works without entitlements for generic passwords; if it returns `errSecMissingEntitlement` (-34018), mark `KeychainTokenStoreTest` `@Ignore` with that reason and cover it in Task 11's manual run.)

- [ ] **Step 2: Run them and see them fail**

Run: `./gradlew :core:datastore:iosSimulatorArm64Test` → FAIL.

- [ ] **Step 3: Implement the Interfaces**

- [ ] **Step 4: Run the tests and build Android**

Run: `./gradlew :core:datastore:iosSimulatorArm64Test :core:datastore:assemble` → PASS / BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

`feat(core): preferences DataStore and secure token store`

---

### Task 5: `core:network`

**Files:**
- Create: `core/network/build.gradle.kts` (`pos.kmp.library`, `pos.koin`, `org.jetbrains.kotlin.plugin.serialization`; deps `:core:common`, `:core:model`, `:core:datastore`, Ktor core/content-negotiation/serialization/logging, `kermit`; `androidMain` okhttp; `iosMain` darwin; `commonTest` `ktor-client-mock`, `:core:testing` is **not** used here)
- Create under `src/commonMain/kotlin/pk/paksoft/pos/core/network/`: `PosJson.kt`, `MoneySerializer.kt`, `ActiveShopProvider.kt`, `AuthTokenProvider.kt`, `SessionEvents.kt`, `ApiEndpoint.kt`, `ApiCaller.kt`, `PosHttpClient.kt`, `dto/{Envelopes.kt,SessionDtos.kt,BootstrapDtos.kt}`, `api/{AuthApi.kt,KtorAuthApi.kt,BootstrapApi.kt,KtorBootstrapApi.kt}`, `di/NetworkModule.kt`, `di/PlatformNetworkModule.kt` (+ android/ios actuals)
- Create: `src/commonTest/kotlin/pk/paksoft/pos/core/network/{ApiFixtures.kt,DtoDecodingTest.kt,MoneySerializerTest.kt,ApiCallerTest.kt,PosHttpClientTest.kt}`

**Interfaces:**
- Consumes: `AppResult`, `AppError` (Task 2), `Money` (Task 2), `AppPreferences.deviceId()` (Task 4), `AppConfig` (Task 2).
- Produces:
  - `val PosJson: Json` (`ignoreUnknownKeys = true`, `explicitNulls = false`, `encodeDefaults = true`).
  - `object MoneySerializer : KSerializer<Money>` (JSON string ↔ `Money.parse`/`toPlainString`).
  - `interface ActiveShopProvider { suspend fun currentApiBaseUrl(): String? }`, `interface AuthTokenProvider { suspend fun token(): String? }` (implemented in `core:data`, Task 8).
  - `@Singleton class SessionEvents { val unauthenticated: SharedFlow<Unit>; fun emitUnauthenticated() }` (replay 0, extraBufferCapacity 1).
  - `@Singleton class ApiEndpoint(private val shops: ActiveShopProvider) { suspend fun url(path: String): String }` → `"<base without trailing />/<path without leading />"`; throws `NoActiveShopException` when no shop.
  - `@Singleton class ApiCaller(private val json: Json, private val events: SessionEvents) { suspend fun <T> call(serializer: KSerializer<T>, request: suspend () -> HttpResponse): AppResult<T>; suspend fun callUnit(request: suspend () -> HttpResponse): AppResult<Unit> }`. 2xx → decode `DataEnvelope(serializer)` and return `data` (`callUnit` ignores the body). Non-2xx with an `ErrorEnvelopeDto` → by `code`: `unauthenticated` → `Unauthenticated` (and `events.emitUnauthenticated()`); `forbidden`, `account_inactive`, `role_not_supported` → `Forbidden(code, message)`; `not_found` → `NotFound`; `validation_failed` → `Validation(message, fields ?: emptyMap())`; `conflict` → `Conflict(message, details?.toString())`; `bad_request`, `too_many_requests`, `server_error`, `maintenance` → `Server(status, message)`; any other code → `Business(code, message, details?.toString())`. Non-2xx without a parsable envelope → `Server(status, null)`. `HttpRequestTimeoutException`, `ConnectTimeoutException`, `SocketTimeoutException` → `Timeout`; other `kotlinx.io.IOException` → `Offline`; `NoActiveShopException`, `SerializationException` and anything else → `Unexpected(e)`; `CancellationException` is rethrown.
  - `fun createPosHttpClient(engine: HttpClientEngine, json: Json, tokens: AuthTokenProvider, prefs: AppPreferences, config: AppConfig): HttpClient` with `expectSuccess = false`, ContentNegotiation(json), HttpTimeout (connect 15 000 ms, request 30 000 ms, socket 30 000 ms), a custom plugin that on every request sets `Accept: application/json`, `Accept-Language: en`, `User-Agent: PaksoftPOS/<appVersion> (<platformName> <osVersion>)`, `X-Device-Id: <prefs.deviceId()>`, and `Authorization: Bearer <token>` read from `tokens.token()` **on every request** unless the request has the `SkipAuth` attribute; `Logging` (level `INFO`, `sanitizeHeader { it == HttpHeaders.Authorization }`, Kermit logger) only when `config.isDebug`. `val SkipAuth: AttributeKey<Boolean>`.
  - DTOs (all `@Serializable`, every property with `@SerialName`): `DataEnvelope<T>(data: T)`, `ErrorEnvelopeDto(error: ErrorBodyDto)`, `ErrorBodyDto(code: String, message: String, fields: Map<String, List<String>>? = null, details: JsonObject? = null)`, `LoginRequestDto(login, password, deviceName→"device_name")`, `SessionDto(token: String? = null, user: UserDto, permissions: List<String>, staffScope: String→"staff_scope")`, `UserDto(id: Long, name: String, email: String, phone: String?, companyName: String?, role: RoleDto, warehouseId: Long?, billerId: Long?)`, `RoleDto(id: Int, name: String)`, `BootstrapDto(shop: ShopDto, pos: PosDto, warehouses: List<WarehouseDto>, billers: List<BillerDto>, customerGroups: List<CustomerGroupDto>, categories: List<CategoryDto>, brands: List<BrandDto>, units: List<UnitDto>, taxes: List<TaxDto>, coupons: List<CouponDto>, accounts: List<AccountDto>)` with nested DTOs whose fields are exactly the A4 keys (money fields typed `Money`/`Money?` with `MoneySerializer`; `CouponDto.amount` is a `String` because it can be a percentage).
  - `interface AuthApi { suspend fun login(request: LoginRequestDto): AppResult<SessionDto>; suspend fun logout(): AppResult<Unit>; suspend fun me(): AppResult<SessionDto> }`, `@Singleton class KtorAuthApi(client: HttpClient, caller: ApiCaller, endpoint: ApiEndpoint) : AuthApi` (paths `auth/login` with `SkipAuth`, `auth/logout`, `me`).
  - `interface BootstrapApi { suspend fun bootstrap(): AppResult<BootstrapDto> }`, `@Singleton class KtorBootstrapApi(…) : BootstrapApi` (path `bootstrap`).
  - `NetworkModule` (triad, `includes = [PlatformNetworkModule::class]`): `@Singleton fun json(): Json = PosJson`, `@Singleton fun httpClient(engine: HttpClientEngine, json: Json, tokens: AuthTokenProvider, prefs: AppPreferences, @Provided config: AppConfig): HttpClient`. `PlatformNetworkModule`: Android `OkHttp.create()`, iOS `Darwin.create()`.
  - `ApiFixtures` (commonTest, `object`): `LOGIN_200`, `ME_200`, `BOOTSTRAP_200` = the JSON examples of `API.md` A1, A3, A4 copied verbatim; `ERROR_INVALID_CREDENTIALS`, `ERROR_VALIDATION_DEVICE_NAME`, `ERROR_UNAUTHENTICATED` = error envelopes using these server messages: invalid_credentials "The username/email or password is incorrect.", validation_failed "Please check the highlighted fields." (with `fields.device_name = ["The device name field is required."]`), unauthenticated "Your session ended. Please log in again.", server_error "Something went wrong on the server. Please try again.".

- [ ] **Step 1: Write the failing tests**

```kotlin
class DtoDecodingTest {
    @Test fun `login example decodes`() {
        val s = PosJson.decodeFromString(DataEnvelope.serializer(SessionDto.serializer()), ApiFixtures.LOGIN_200).data
        assertEquals("12|Xn3r…", s.token); assertEquals(7, s.user.id); assertEquals("Admin", s.user.role.name)
        assertEquals("all", s.staffScope); assertTrue("sales-add" in s.permissions); assertNull(s.user.warehouseId)
    }
    @Test fun `me example has no token`() { assertNull(PosJson.decodeFromString(DataEnvelope.serializer(SessionDto.serializer()), ApiFixtures.ME_200).data.token) }
    @Test fun `bootstrap example decodes`() {
        val b = PosJson.decodeFromString(DataEnvelope.serializer(BootstrapDto.serializer()), ApiFixtures.BOOTSTRAP_200).data
        assertEquals("PKR", b.shop.currency?.code); assertEquals("prefix", b.shop.currency?.position); assertTrue(b.shop.features.ledger)
        assertEquals(20, b.pos.pageSize); assertEquals("10.00", b.coupons.single().amount); assertNull(b.coupons.single().minimumAmount)
        assertEquals(17.0, b.taxes.single().rate)
    }
    @Test fun `unknown keys are ignored`() { /* LOGIN_200 with an extra "new_field": 1 inside user still decodes */ }
}

class MoneySerializerTest {
    @Serializable data class Box(@Serializable(with = MoneySerializer::class) val m: Money?)
    @Test fun `round trip`() { assertEquals("""{"m":"7998.00"}""", PosJson.encodeToString(Box.serializer(), Box(Money(799800)))); assertEquals(Money(5), PosJson.decodeFromString(Box.serializer(), """{"m":"0.05"}""").m) }
    @Test fun `null stays null`() { assertNull(PosJson.decodeFromString(Box.serializer(), """{"m":null}""").m) }
}

class ApiCallerTest {
    // helper: fun respond(status, body, contentType = Json) → AppResult<SessionDto> via MockEngine + ApiCaller
    @Test fun `200 returns data`() { /* LOGIN_200 → Success with user.id 7 */ }
    @Test fun `204 unit call succeeds`() { /* callUnit on 204 empty → Success(Unit) */ }
    @Test fun `401 unauthenticated emits session event`() = runTest { /* ERROR_UNAUTHENTICATED → Failure(Unauthenticated); events.unauthenticated received one item (Turbine) */ }
    @Test fun `401 invalid credentials is a business error without session event`() { /* → Business("invalid_credentials", "The username/email or password is incorrect.", null); no event */ }
    @Test fun `account inactive is forbidden`() { /* 403 account_inactive → Forbidden("account_inactive", message) */ }
    @Test fun `422 carries field errors`() { /* ERROR_VALIDATION_DEVICE_NAME → Validation with fields["device_name"] non-empty */ }
    @Test fun `html 404 becomes Server 404 without message`() { /* 404, "<html>Not Found</html>", text/html → Server(404, null) */ }
    @Test fun `500 envelope keeps server message`() { /* server_error → Server(500, "Something went wrong on the server. Please try again.") */ }
    @Test fun `timeout maps to Timeout`() { /* engine throws HttpRequestTimeoutException("u", 1) → Timeout */ }
    @Test fun `io failure maps to Offline`() { /* engine throws kotlinx.io.IOException("no route") → Offline */ }
}

class PosHttpClientTest {
    // builds createPosHttpClient(MockEngine{ capture request }, PosJson, FakeTokens, FakePrefs(deviceId="dev-1"), AppConfig(false,"Android","16","0.1.0","http://l"))
    @Test fun `adds standard headers`() { /* Accept application/json, Accept-Language en, User-Agent "PaksoftPOS/0.1.0 (Android 16)", X-Device-Id dev-1 */ }
    @Test fun `adds bearer token when present`() { /* Authorization "Bearer t1" */ }
    @Test fun `skips auth header for login`() { /* request with attributes.put(SkipAuth, true) → no Authorization */ }
    @Test fun `token is read per request for the active shop`() { /* FakeTokens returns "a" then "b" → first request "Bearer a", second "Bearer b" */ }
    @Test fun `endpoint joins base url and path`() = runTest { /* ApiEndpoint(shop base "https://x/api/v1/").url("/me") == "https://x/api/v1/me"; no shop → NoActiveShopException */ }
}
```

Fill each commented test with the assertions its comment states. Small fakes for `AuthTokenProvider` and `AppPreferences` live in the test file.

- [ ] **Step 2: Run them and see them fail**

Run: `./gradlew :core:network:iosSimulatorArm64Test` → FAIL.

- [ ] **Step 3: Implement the Interfaces**

- [ ] **Step 4: Run the tests on both platforms**

Run: `./gradlew :core:network:iosSimulatorArm64Test :core:network:testAndroidHostTest` → PASS.

- [ ] **Step 5: Update the architecture doc**

`ARCHITECTURE.md` §5: replace the `Auth { bearer { … } }` bullet with "a small custom plugin adds `Authorization: Bearer <token>` read from `AuthTokenProvider` on every request (no token caching, so switching shop or logging out takes effect immediately); requests marked `SkipAuth` (login) get no header".

- [ ] **Step 6: Commit**

`feat(core): Ktor client, error mapping, auth and bootstrap APIs`

---

### Task 6: `core:database`

**Files:**
- Create: `core/database/build.gradle.kts` (`pos.kmp.library`, `pos.koin`, `pos.room`; deps `:core:common`), `src/commonMain/kotlin/pk/paksoft/pos/core/database/{PosDatabase.kt,entity/BootstrapEntities.kt,dao/BootstrapDao.kt,di/DatabaseModule.kt,di/PlatformDatabaseModule.kt}` (+ android/ios actual modules), `core/database/schemas/` (generated)
- Test: `core/database/src/iosTest/kotlin/pk/paksoft/pos/core/database/BootstrapDaoTest.kt`

**Interfaces:**
- Produces:
  - Entities (table names in quotes; money as `Long` paisa): `ShopSettingsEntity` `"shop_settings"` (`id: Int = 1` PK, `title`, `logoUrl`, `developedBy`, `dateFormat`, `invoiceFormat`, `gstState: Int?`, `timezone`, `currencyId: Long?`, `currencyCode: String?`, `currencyName: String?`, `exchangeRate: Double?`, `currencyPosition: String?`, `featureLedger: Boolean`, `featurePreviousBalance: Boolean`, `posCustomerId: Long?`, `posWarehouseId: Long?`, `posBillerId: Long?`, `posPageSize: Int?`, `posKeyboardActive: Boolean`), `WarehouseEntity` `"warehouses"`, `BillerEntity` `"billers"`, `CustomerGroupEntity` `"customer_groups"`, `CategoryEntity` `"categories"`, `BrandEntity` `"brands"`, `UnitEntity` `"units"`, `TaxEntity` `"taxes"`, `CouponEntity` `"coupons"` (`type: String`, `amountHundredths: Long` = fixed paisa or percent × 100, `minimumAmountPaisa: Long?`, `remaining: Int`, `expiredDate: String`), `AccountEntity` `"accounts"`; each list entity has `id: Long` PK and the A4 fields.
  - `@Dao abstract class BootstrapDao`: `@Transaction open suspend fun replaceAll(settings: ShopSettingsEntity, warehouses: List<WarehouseEntity>, billers: List<BillerEntity>, customerGroups: List<CustomerGroupEntity>, categories: List<CategoryEntity>, brands: List<BrandEntity>, units: List<UnitEntity>, taxes: List<TaxEntity>, coupons: List<CouponEntity>, accounts: List<AccountEntity>)`; `@Transaction open suspend fun clearAll()`; `abstract fun observeSettings(): Flow<ShopSettingsEntity?>`; `abstract fun observeWarehouses(): Flow<List<WarehouseEntity>>`; `abstract fun observeBillers(): Flow<List<BillerEntity>>`; `abstract suspend fun countAll(): Int` (sum of rows in all ten tables, for tests).
  - `@Database(entities = [all ten], version = 1, exportSchema = true) @ConstructedBy(PosDatabaseConstructor::class) abstract class PosDatabase : RoomDatabase { abstract fun bootstrapDao(): BootstrapDao }` + `expect object PosDatabaseConstructor`.
  - `fun buildPosDatabase(builder: RoomDatabase.Builder<PosDatabase>, ioContext: CoroutineContext): PosDatabase` (bundled driver, query context, `fallbackToDestructiveMigration(dropAllTables = true)`; Phase 0 tables are a cache).
  - `DatabaseModule` (triad, includes `PlatformDatabaseModule`): `@Singleton fun database(builder: RoomDatabase.Builder<PosDatabase>, dispatchers: AppDispatchers): PosDatabase`, `@Singleton fun bootstrapDao(db: PosDatabase): BootstrapDao`. Platform builders: Android `Room.databaseBuilder<PosDatabase>(context, context.getDatabasePath("pos.db").absolutePath)`; iOS `Room.databaseBuilder<PosDatabase>(name = <NSDocumentDirectory>/pos.db)`.

- [ ] **Step 1: Write the failing tests**

```kotlin
class BootstrapDaoTest {
    private fun db() = Room.inMemoryDatabaseBuilder<PosDatabase>().setDriver(BundledSQLiteDriver()).build()
    @Test fun `replaceAll stores settings and lists`() = runTest {
        val dao = db().bootstrapDao()
        dao.replaceAll(settings("SMF"), listOf(warehouse(1), warehouse(2)), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals("SMF", dao.observeSettings().first()?.title); assertEquals(listOf(1L, 2L), dao.observeWarehouses().first().map { it.id })
    }
    @Test fun `replaceAll replaces previous rows`() = runTest { /* second replaceAll with only warehouse(3) → observeWarehouses ids == [3] and title from the second settings */ }
    @Test fun `clearAll empties every table`() = runTest { /* after replaceAll then clearAll → countAll() == 0 and observeSettings().first() == null */ }
}
```

- [ ] **Step 2: Run them and see them fail**

Run: `./gradlew :core:database:iosSimulatorArm64Test` → FAIL.

- [ ] **Step 3: Implement the Interfaces**

- [ ] **Step 4: Run tests and build Android**

Run: `./gradlew :core:database:iosSimulatorArm64Test :core:database:assemble` → PASS; `core/database/schemas/pk.paksoft.pos.core.database.PosDatabase/1.json` exists.

- [ ] **Step 5: Commit**

`feat(core): Room database caching bootstrap data`

---

### Task 7: `core:testing`

**Files:**
- Create: `core/testing/build.gradle.kts` (`pos.kmp.library`; deps `:core:common`, `:core:model`, `:core:datastore`, `:core:network`, `:core:data`, `kotlin-test`, `kotlinx-coroutines-test`), `core/testing/src/commonMain/kotlin/pk/paksoft/pos/core/testing/{InMemoryAppPreferences.kt,InMemoryTokenStore.kt,FakeAuthApi.kt,FakeBootstrapApi.kt,FakeLocalDataCleaner.kt,FakeSessionRepository.kt,FakeBootstrapRepository.kt,TestData.kt}`

This task's types depend on `core:data` interfaces, so write it **together with Task 8**: create the files of Task 8's Interfaces first (interfaces only), then this task, then Task 8's tests and implementations. Commit them together at the end of Task 8.

**Interfaces:**
- Consumes: `AppPreferences`, `TokenStore` (Task 4); `AuthApi`, `BootstrapApi`, DTOs (Task 5); `SessionRepository`, `BootstrapRepository`, `LocalDataCleaner` (Task 8).
- Produces:
  - `class InMemoryAppPreferences(activeShopId: String? = null, cachedSessionJson: String? = null, deviceId: String = "device-test") : AppPreferences`.
  - `class InMemoryTokenStore(initial: Map<String, String> = emptyMap()) : TokenStore { val tokens: Map<String, String> }`.
  - `class FakeAuthApi : AuthApi { var loginResult: AppResult<SessionDto>; var meResult: AppResult<SessionDto>; var logoutResult: AppResult<Unit>; val loginCalls: List<LoginRequestDto>; var logoutCalls: Int; var loginGate: CompletableDeferred<Unit>? }` (when `loginGate` is set, `login` suspends until it completes, for double-submit tests).
  - `class FakeBootstrapApi : BootstrapApi { var result: AppResult<BootstrapDto> }`.
  - `class FakeLocalDataCleaner : LocalDataCleaner { var clearCalls: Int }`.
  - `class FakeSessionRepository(initial: SessionState = SessionState.NoShop, shops: List<Shop> = Shops.forBuild(true, "http://local")) : SessionRepository` exposing `val state: MutableStateFlow<SessionState>`, recorded `selectedShopIds`, `loginCalls: List<Pair<String, String>>`, `var loginResult: AppResult<UserSession>`, `var loginGate: CompletableDeferred<Unit>?`, `logoutCalls`, `switchShopCalls`, `startCalls`.
  - `class FakeBootstrapRepository : BootstrapRepository { val settingsFlow: MutableStateFlow<ShopSettings?>; var refreshResult: AppResult<Unit>; var refreshCalls: Int }`.
  - `object TestData { fun sessionDto(token: String? = "tok-1", permissions: List<String> = listOf("sales-index", "sales-add"), roleId: Int = 1): SessionDto; fun userSession(name: String = "cashier1", roleName: String = "Admin", permissions: Set<String> = setOf("sales-index", "sales-add")): UserSession; fun bootstrapDto(title: String = "Super madina Fan", ledger: Boolean = true): BootstrapDto; fun shopSettings(ledger: Boolean = true): ShopSettings; val smf: Shop; val local: Shop }`.

- [ ] **Step 1: Implement the fakes** (no tests of their own; they are exercised by Tasks 8–10)

- [ ] **Step 2: Compile**

Run: `./gradlew :core:testing:compileKotlinIosSimulatorArm64` → BUILD SUCCESSFUL.

---

### Task 8: `core:data` (session and bootstrap repositories)

**Files:**
- Create: `core/data/build.gradle.kts` (`pos.kmp.library`, `pos.koin`, `org.jetbrains.kotlin.plugin.serialization`; `api` deps `:core:model`, `:core:common`; `implementation` `:core:network`, `:core:database`, `:core:datastore`; `commonTest` `:core:testing`)
- Create under `src/commonMain/kotlin/pk/paksoft/pos/core/data/`: `ShopCatalog.kt`, `PrefsActiveShopProvider.kt`, `StoredTokenProvider.kt`, `LocalDataCleaner.kt`, `RoomLocalDataCleaner.kt`, `SessionRepository.kt`, `DefaultSessionRepository.kt`, `BootstrapRepository.kt`, `DefaultBootstrapRepository.kt`, `mapper/SessionMappers.kt`, `mapper/BootstrapMappers.kt`, `di/DataModule.kt`
- Test: `src/commonTest/kotlin/pk/paksoft/pos/core/data/{DefaultSessionRepositoryTest.kt,SessionMappersTest.kt,BootstrapMappersTest.kt}`, `src/iosTest/kotlin/pk/paksoft/pos/core/data/DefaultBootstrapRepositoryTest.kt`

**Interfaces:**
- Consumes: Tasks 2, 4, 5, 6, 7.
- Produces:
  - `@Singleton class ShopCatalog(@Provided config: AppConfig) { val shops: List<Shop>; fun byId(id: String?): Shop? }`.
  - `@Singleton class PrefsActiveShopProvider(prefs: AppPreferences, catalog: ShopCatalog) : ActiveShopProvider`; `@Singleton class StoredTokenProvider(prefs: AppPreferences, tokens: TokenStore) : AuthTokenProvider` (token of the **current** active shop, read each call).
  - `interface LocalDataCleaner { suspend fun clearAll() }`; `@Singleton internal class RoomLocalDataCleaner(dao: BootstrapDao) : LocalDataCleaner`.
  - `interface SessionRepository { val state: StateFlow<SessionState>; val shops: List<Shop>; suspend fun start(); suspend fun selectShop(shopId: String); suspend fun login(login: String, password: String): AppResult<UserSession>; suspend fun refreshSession(): AppResult<UserSession>; suspend fun logout(); suspend fun switchShop() }`.
  - `@Singleton class DefaultSessionRepository(prefs: AppPreferences, tokens: TokenStore, catalog: ShopCatalog, authApi: AuthApi, cleaner: LocalDataCleaner, events: SessionEvents, scope: ApplicationScope, @Provided config: AppConfig) : SessionRepository`. Behavior:
    - initial state `Starting`; collects `events.unauthenticated` in `scope` → if `LoggedIn`, expire (clear token for that shop, `prefs.setCachedSessionJson(null)`, `cleaner.clearAll()`, state `LoggedOut(shop, SessionExpired)`).
    - `start()`: no/unknown active shop → `NoShop`; shop without token → `LoggedOut(shop, null)`; token + cached session JSON → `LoggedIn(shop, cached)` immediately, then `refreshSession()` (success updates the cache and state; `Offline`/`Timeout`/`Server` leave the cached session in place); token without cache → `refreshSession()` and `LoggedIn` on success, `LoggedOut` otherwise.
    - `selectShop(id)`: `prefs.setActiveShopId(id)`; state `LoggedOut(shop, null)`.
    - `login(login, password)`: sends `LoginRequestDto(login.trim(), password, deviceName = "${config.platformName} (PAKSOFT POS ${config.appVersion})")`; on success writes the token for the active shop, caches the session JSON (the `SessionDto` with `token = null`, encoded with `PosJson`), state `LoggedIn`. On failure state stays `LoggedOut` and nothing is stored.
    - `logout()`: calls `authApi.logout()` and ignores its result; clears token, cached session and local data; state `LoggedOut(shop, null)`.
    - `switchShop()`: if logged in, do everything `logout()` does; then `prefs.setActiveShopId(null)`; state `NoShop`.
  - `fun SessionDto.toUserSession(): UserSession` (`staff_scope` `"all"`/`"own"` → `StaffScope.All`/`Own`, else `Unknown`).
  - `interface BootstrapRepository { val settings: Flow<ShopSettings?>; suspend fun refresh(): AppResult<Unit> }`; `@Singleton class DefaultBootstrapRepository(api: BootstrapApi, dao: BootstrapDao) : BootstrapRepository` (`refresh` maps DTO → entities and calls `dao.replaceAll` in one call; `settings` maps `observeSettings()` to `ShopSettings`).
  - Mappers `BootstrapDto.toEntities()` (returns a data class `BootstrapEntities` holding all ten parts) and `ShopSettingsEntity.toModel(): ShopSettings`. Coupon: `"fixed"` → `amountHundredths = Money.parse(amount).paisa`; `"percentage"` → `(amount.toDouble() * 100).roundToLong()`.
  - `DataModule`: triad for `pk.paksoft.pos.core.data`.

- [ ] **Step 1: Write the interfaces (`SessionRepository`, `BootstrapRepository`, `LocalDataCleaner`), then complete Task 7**

- [ ] **Step 2: Write the failing tests**

```kotlin
class DefaultSessionRepositoryTest {
    // repo(prefs, tokens, api, cleaner, events) helper builds DefaultSessionRepository with ApplicationScope(test dispatchers via StandardTestDispatcher(testScheduler)) and AppConfig(true, "Android", "16", "0.1.0", "http://10.0.2.2:8010/api/v1")
    @Test fun `start without shop is NoShop`() = runTest { /* prefs empty → state NoShop */ }
    @Test fun `start with shop and no token is LoggedOut`() = runTest { /* active "smf" → LoggedOut(smf, null) */ }
    @Test fun `start offline with cached session stays logged in`() = runTest {
        // tokens {"smf": "t"}, cached session JSON of TestData.sessionDto(token = null), api.meResult = Failure(Offline)
        // → state LoggedIn(smf, …) after start() and after advanceUntilIdle(); token still "t"
    }
    @Test fun `login trims and stores token for the active shop`() = runTest {
        // active "local", api.loginResult = Success(TestData.sessionDto(token = "tok-9"))
        // login("  cashier1  ", "pw") → Success; api.loginCalls.single().login == "cashier1"; deviceName == "Android (PAKSOFT POS 0.1.0)"
        // tokens.tokens["local"] == "tok-9"; state is LoggedIn; prefs cached session JSON contains no "tok-9"
    }
    @Test fun `failed login stores nothing`() = runTest { /* loginResult Failure(Business("invalid_credentials", …)) → state LoggedOut, tokens empty, cache null */ }
    @Test fun `unauthenticated event expires the session`() = runTest {
        // logged in via login(); events.emitUnauthenticated(); advanceUntilIdle()
        // → state LoggedOut(local, SessionExpired); token cleared; cleaner.clearCalls == 1; cache null
    }
    @Test fun `logout clears even when the server call fails`() = runTest { /* logoutResult Failure(Offline) → logoutCalls 1, token cleared, clearCalls 1, LoggedOut(shop, null) */ }
    @Test fun `switch shop clears token and local data`() = runTest { /* logged in on "smf" → switchShop() → token for smf gone, clearCalls 1, prefs active shop null, state NoShop */ }
    @Test fun `select shop goes to login for that shop`() = runTest { /* selectShop("bsf") → LoggedOut(bsf, null) and prefs active "bsf" */ }
}

class SessionMappersTest { @Test fun `staff scope own maps to Own`() { /* sessionDto with staffScope "own" → StaffScope.Own; "weird" → Unknown; permissions become a set */ } }

class BootstrapMappersTest {
    @Test fun `percentage coupon keeps hundredths of a percent`() { /* coupon type percentage amount "10.00" → amountHundredths 1000 */ }
    @Test fun `fixed coupon becomes paisa`() { /* type fixed amount "250.50" → 25050 */ }
    @Test fun `settings entity round trips to model`() { /* bootstrapDto().toEntities().settings.toModel() has title, currency PKR Prefix, features.ledger, pos.pageSize 20 */ }
}

// iosTest
class DefaultBootstrapRepositoryTest {
    @Test fun `refresh stores data and settings flow emits it`() = runTest { /* in-memory db + FakeBootstrapApi(Success(TestData.bootstrapDto("SMF"))) → refresh() Success; settings.first()?.title == "SMF" */ }
    @Test fun `failed refresh keeps the cached data`() = runTest { /* first refresh success, then api.result = Failure(Offline) → refresh() Failure(Offline); settings.first()?.title still "SMF" */ }
}
```

- [ ] **Step 3: Run them and see them fail**

Run: `./gradlew :core:data:iosSimulatorArm64Test` → FAIL.

- [ ] **Step 4: Implement**

- [ ] **Step 5: Run the tests**

Run: `./gradlew :core:data:iosSimulatorArm64Test :core:data:testAndroidHostTest` → PASS (the iOS-only bootstrap test runs on the simulator).

- [ ] **Step 6: Commit (with Task 7)**

`git add core/testing core/data settings.gradle.kts && git commit -m "feat(core): session and bootstrap repositories with test fakes"`

---

### Task 9: `feature:auth` (shop picker and login)

**Files:**
- Create: `feature/auth/build.gradle.kts` (`pos.kmp.feature`), `src/commonMain/kotlin/pk/paksoft/pos/feature/auth/{shop/ShopPickerViewModel.kt,shop/ShopPickerUiState.kt,shop/ShopPickerRoute.kt,shop/ShopPickerScreen.kt,login/LoginViewModel.kt,login/LoginUiState.kt,login/LoginRoute.kt,login/LoginScreen.kt,di/AuthModule.kt}`, `src/commonMain/composeResources/values/strings.xml`
- Test: `src/commonTest/kotlin/pk/paksoft/pos/feature/auth/{ShopPickerViewModelTest.kt,LoginViewModelTest.kt}`

**Interfaces:**
- Consumes: `SessionRepository`, `FakeSessionRepository`, `UiText`, `toUiText()`, design system components.
- Produces:
  - `data class ShopItem(val id: String, val name: String, val isDevelopment: Boolean)`; `data class ShopPickerUiState(val shops: List<ShopItem>)`; `sealed interface ShopPickerAction { data class ShopClicked(val id: String) }`; `@KoinViewModel class ShopPickerViewModel(session: SessionRepository)` with `val state: StateFlow<ShopPickerUiState>` and `fun onAction(action: ShopPickerAction)`.
  - `data class LoginUiState(val shopName: String = "", val login: String = "", val password: String = "", val passwordVisible: Boolean = false, val isSubmitting: Boolean = false, val loginError: UiText? = null, val passwordError: UiText? = null, val banner: UiText? = null)`; `sealed interface LoginAction { data class LoginChanged(val value: String); data class PasswordChanged(val value: String); data object TogglePasswordVisibility; data object Submit; data object ChangeShop }`; `@KoinViewModel class LoginViewModel(session: SessionRepository)`.
    - The ViewModel outlives logins (it is created outside `NavDisplay`), so it **collects `session.state`**: each time the state becomes `LoggedOut(shop, notice)` it sets `shopName = shop.displayName`, clears `password`, `isSubmitting`, `loginError`, `passwordError`, keeps `login` only when the shop is the same as before, and sets `banner = UiText.Res(session_expired)` when `notice == SessionExpired` (else `null`).
    - Submit: blank (after trim) login → `loginError = login_error_login_required`; blank password → `passwordError = login_error_password_required`; no call in either case. While `isSubmitting`, Submit is ignored. Typing clears that field's error and the banner. On `Validation` failure: `loginError` from `fields["login"]?.first()`, `passwordError` from `fields["password"]?.first()`, banner = message when neither field matched. Other failures: `banner = error.toUiText()`. Success needs no navigation (the session gate in `composeApp` reacts to `SessionState`).
    - ChangeShop → `session.switchShop()`.
  - `@Composable fun ShopPickerRoute(vm: ShopPickerViewModel = koinViewModel())`, `@Composable fun LoginRoute(vm: LoginViewModel = koinViewModel())`; stateless `ShopPickerScreen(state, onAction)` (title, subtitle, a card per shop; dev shops show a `Dev` chip) and `LoginScreen(state, onAction)` (shop label, two `PosTextField`s, Show/Hide text button for the password, `PosButton(loading = isSubmitting)`, "Change shop" text button, `ErrorBanner` for `banner`; Done on the password field submits). Phone layout centered with max width 420.dp so it also looks right on tablets. Previews for both screens including the error state.

- [ ] **Step 1: Write the failing tests**

```kotlin
class ShopPickerViewModelTest {
    @Test fun `lists every shop with dev flag`() = runTest { /* FakeSessionRepository default shops → state.shops ids smf,bsf,mummyandme,staging,local; local.isDevelopment true */ }
    @Test fun `clicking a shop selects it`() = runTest { /* onAction(ShopClicked("bsf")) → fake.selectedShopIds == ["bsf"] */ }
}

class LoginViewModelTest {
    // vm built with FakeSessionRepository(initial = LoggedOut(TestData.smf, null)); Dispatchers.setMain(UnconfinedTestDispatcher()) in @BeforeTest
    @Test fun `shows the shop name`() = runTest { assertEquals("Super Madina Solar Fan", vm.state.value.shopName) }
    @Test fun `blank fields show errors and do not call login`() = runTest { /* Submit with "" and "" → loginError Res(login_error_login_required), passwordError Res(login_error_password_required), fake.loginCalls empty */ }
    @Test fun `second submit while submitting is ignored`() = runTest {
        fake.loginGate = CompletableDeferred()
        vm.onAction(LoginChanged("cashier1")); vm.onAction(PasswordChanged("pw"))
        vm.onAction(Submit); vm.onAction(Submit)
        assertTrue(vm.state.value.isSubmitting); fake.loginGate!!.complete(Unit); advanceUntilIdle()
        assertEquals(1, fake.loginCalls.size)
    }
    @Test fun `validation errors land on their fields`() = runTest { /* loginResult Failure(Validation("Please check…", mapOf("login" to listOf("Login is required.")))) → loginError Raw("Login is required."), banner null */ }
    @Test fun `invalid credentials shows the server message`() = runTest { /* Business("invalid_credentials", "The username/email or password is incorrect.", null) → banner Raw(that) */ }
    @Test fun `html 404 shows shop not connected banner`() = runTest { /* Failure(Server(404, null)) → banner Res(error_shop_not_connected) */ }
    @Test fun `offline shows offline banner`() = runTest { /* Failure(Offline) → banner Res(error_offline) */ }
    @Test fun `expired notice shows banner`() = runTest { /* initial LoggedOut(smf, SessionExpired) → banner Res(session_expired) */ }
    @Test fun `typing clears the field error`() = runTest { /* after blank submit, LoginChanged("a") → loginError null */ }
    @Test fun `change shop switches shop`() = runTest { /* ChangeShop → fake.switchShopCalls == 1 */ }
    @Test fun `logging out again clears the password and keeps the login`() = runTest { /* type "cashier1"/"pw"; fake.state = LoggedIn(smf, …); fake.state = LoggedOut(smf, null) → password "", login "cashier1", isSubmitting false */ }
    @Test fun `a different shop clears the login too`() = runTest { /* type "cashier1"; fake.state = LoggedOut(TestData.local, null) → login "", shopName "Local API (dev)" */ }
}
```

- [ ] **Step 2: Run them and see them fail**

Run: `./gradlew :feature:auth:iosSimulatorArm64Test` → FAIL.

- [ ] **Step 3: Implement**

- [ ] **Step 4: Run the tests**

Run: `./gradlew :feature:auth:iosSimulatorArm64Test :feature:auth:testAndroidHostTest` → PASS.

- [ ] **Step 5: Commit**

`feat(auth): shop picker and login screens`

---

### Task 10: `feature:settings` ("More" screen)

**Files:**
- Create: `feature/settings/build.gradle.kts` (`pos.kmp.feature`), `src/commonMain/kotlin/pk/paksoft/pos/feature/settings/{MoreViewModel.kt,MoreUiState.kt,MoreRoute.kt,MoreScreen.kt,navigation/SettingsKeys.kt,navigation/SettingsEntries.kt,di/SettingsModule.kt}`, `src/commonMain/composeResources/values/strings.xml`
- Test: `src/commonTest/kotlin/pk/paksoft/pos/feature/settings/MoreViewModelTest.kt`

**Interfaces:**
- Consumes: `SessionRepository`, `BootstrapRepository`, fakes.
- Produces:
  - `@Serializable data object MoreKey : NavKey`; `fun EntryProviderScope<NavKey>.settingsEntries()` registering `entry<MoreKey> { MoreRoute() }`.
  - `data class MoreUiState(val userName: String = "", val roleName: String = "", val shopName: String = "", val isRefreshing: Boolean = false, val message: UiText? = null)`; `sealed interface MoreAction { data object RefreshData; data object SwitchShop; data object Logout }`; `@KoinViewModel class MoreViewModel(session: SessionRepository, bootstrap: BootstrapRepository)`. RefreshData: `isRefreshing` while running; success → `message = more_refreshed`; failure → `message = error.toUiText()`; a second RefreshData while refreshing is ignored. `shopName` = `ShopSettings.title` when cached, else the `Shop.displayName`.
  - `MoreRoute(vm: MoreViewModel = koinViewModel())`, `MoreScreen(state, onAction)` with `PosTopBar(more_title)`, "Signed in as …", "Shop: …", buttons Refresh shop data / Switch shop / Log out (outlined, danger color for Log out), message shown in a snackbar-style `ErrorBanner` or info text. Preview.

- [ ] **Step 1: Write the failing tests**

```kotlin
class MoreViewModelTest {
    @Test fun `shows user role and shop title`() = runTest { /* LoggedIn(smf, userSession named "cashier1" role "Admin") + settings title "Super madina Fan" → userName cashier1, roleName Admin, shopName "Super madina Fan" */ }
    @Test fun `falls back to shop display name without cached settings`() = runTest { /* settings null → shopName "Super Madina Solar Fan" */ }
    @Test fun `refresh success shows updated message`() = runTest { /* refreshResult Success → message Res(more_refreshed), isRefreshing false, refreshCalls 1 */ }
    @Test fun `refresh failure shows error`() = runTest { /* Failure(Offline) → message Res(error_offline) */ }
    @Test fun `logout and switch delegate to session`() = runTest { /* Logout → logoutCalls 1; SwitchShop → switchShopCalls 1 */ }
}
```

- [ ] **Step 2: Run them and see them fail** — `./gradlew :feature:settings:iosSimulatorArm64Test` → FAIL.
- [ ] **Step 3: Implement**
- [ ] **Step 4: Run the tests** — `./gradlew :feature:settings:iosSimulatorArm64Test :feature:settings:testAndroidHostTest` → PASS.
- [ ] **Step 5: Commit** — `feat(settings): More screen with refresh, switch shop and logout`

---

### Task 11: Wire the app (Koin, session gate, navigation) and run it end to end

**Files:**
- Modify: `composeApp/build.gradle.kts` (`pos.kmp.compose`, `pos.koin`, `org.jetbrains.kotlin.plugin.serialization`; deps on every `core:*` module except `core:testing`, `feature:auth`, `feature:settings`, lifecycle, navigation3-ui, lifecycle-viewmodel-navigation3; `export` not needed), `composeApp/src/commonMain/kotlin/pk/paksoft/pos/app/App.kt`
- Create: `composeApp/src/commonMain/kotlin/pk/paksoft/pos/app/{KoinInit.kt,AppViewModel.kt,main/MainScaffold.kt,main/MainViewModel.kt,main/TopLevelDestination.kt,main/ComingSoonScreen.kt,navigation/TopLevelKeys.kt,navigation/NavSavedState.kt,di/AppModule.kt}`, `composeApp/src/iosMain/kotlin/pk/paksoft/pos/app/KoinInitIos.kt`, `composeApp/src/commonMain/composeResources/values/strings.xml`
- Create: `androidApp/src/main/kotlin/pk/paksoft/pos/android/PosApplication.kt`, `androidApp/src/debug/AndroidManifest.xml`, `androidApp/src/debug/res/xml/network_security_config.xml`
- Modify: `androidApp/src/main/AndroidManifest.xml` (`android:name=".PosApplication"`), `iosApp/iosApp/iOSApp.swift` (call `KoinInitIosKt.doInitKoinIos()` in `init()`)
- Test: `composeApp/src/commonTest/kotlin/pk/paksoft/pos/app/{TopLevelDestinationTest.kt,AppViewModelTest.kt,MainViewModelTest.kt}` (composeApp `commonTest` depends on `:core:testing`)
- Modify: `README.md` (Status), `docs/ARCHITECTURE.md` (anything that changed during Phase 0)

**Interfaces:**
- Consumes: everything above.
- Produces:
  - `@KoinApplication object PosKoinApp`; `fun initKoin(config: AppConfig, platform: KoinAppDeclaration? = null)` → `startKoin<PosKoinApp> { koin.declare(config); platform?.invoke(this) }` (`org.koin.plugin.module.dsl.startKoin`).
  - iOS `fun initKoinIos()` → `initKoin(AppConfig(isDebug = Platform.isDebugBinary, platformName = "iOS", osVersion = UIDevice.currentDevice.systemVersion, appVersion = <CFBundleShortVersionString>, localApiBaseUrl = "http://127.0.0.1:8010/api/v1"))`.
  - Android `PosApplication.onCreate()` → `initKoin(AppConfig(BuildConfig.DEBUG, "Android", Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, "http://10.0.2.2:8010/api/v1")) { androidContext(this@PosApplication) }`.
  - `@KoinViewModel class AppViewModel(session: SessionRepository) { val state: StateFlow<SessionState> }` (calls `session.start()` once in `init`).
  - `App()`: `PosTheme { when (state) { Starting → LoadingState(); NoShop → ShopPickerRoute(); is LoggedOut → LoginRoute() (keyed by shop id); is LoggedIn → MainScaffold(state.session) } }`.
  - Keys: `@Serializable data object DashboardKey : NavKey`, `PosKey`, `SalesKey`, `LedgerKey` (composeApp) and `MoreKey` (settings). `val navSavedStateConfig: SavedStateConfiguration` registering all five in a polymorphic `NavKey` `SerializersModule`.
  - `enum class TopLevelDestination(val key: NavKey, val label: StringResource, val icon: ImageVector, val requires: Set<String>, val needsLedger: Boolean)`: DASHBOARD(DashboardKey, nav_dashboard, `Icons.Filled.Home`, ∅, false), POS(PosKey, nav_pos, `Icons.Filled.ShoppingCart`, {sales-add}, false), SALES(SalesKey, nav_sales, `Icons.AutoMirrored.Filled.List`, {sales-index}, false), LEDGER(LedgerKey, nav_ledger, `Icons.Filled.Edit`, {sales-index}, true), MORE(MoreKey, nav_more, `Icons.Filled.Menu`, ∅, false). `fun visibleDestinations(permissions: Set<String>, ledgerEnabled: Boolean): List<TopLevelDestination>`.
  - `@KoinViewModel class MainViewModel(bootstrap: BootstrapRepository, session: SessionRepository)`: it outlives logins, so it collects `session.state` and calls `bootstrap.refresh()` **every time the state changes from anything else to `LoggedIn`**; `state: StateFlow<MainUiState(settings: ShopSettings?, banner: UiText?)>`; refresh failure (other than `Unauthenticated`) → banner; a new login clears the old banner.
  - `MainScaffold(session: UserSession, vm: MainViewModel = koinViewModel())`: `NavigationSuiteScaffold`-style layout (bottom bar on compact width, rail on wider) built from `visibleDestinations(session.permissions, settings?.features?.ledger ?: false)`; one back stack `rememberNavBackStack(navSavedStateConfig, DashboardKey)`; selecting a tab clears the stack and adds that key; `NavDisplay` with `rememberSaveableStateHolderNavEntryDecorator()` and `rememberViewModelStoreNavEntryDecorator()`; entries: Dashboard/POS/Sales/Ledger → `ComingSoonScreen(title)` (top bar with the tab name and `coming_soon` text), `settingsEntries()`; banner shown above content.

- [ ] **Step 1: Write the failing tests**

```kotlin
class TopLevelDestinationTest {
    @Test fun `admin with ledger sees all five`() { assertEquals(TopLevelDestination.entries.toList(), visibleDestinations(setOf("sales-index", "sales-add"), ledgerEnabled = true)) }
    @Test fun `ledger hidden when the shop has no ledger`() { assertFalse(TopLevelDestination.LEDGER in visibleDestinations(setOf("sales-index", "sales-add"), false)) }
    @Test fun `staff without sales permissions sees dashboard and more only`() { assertEquals(listOf(TopLevelDestination.DASHBOARD, TopLevelDestination.MORE), visibleDestinations(emptySet(), true)) }
}

class AppViewModelTest {
    @Test fun `starts the session once and mirrors its state`() = runTest { /* FakeSessionRepository → startCalls 1; fake.state.value = NoShop → vm.state.value NoShop */ }
}

class MainViewModelTest {
    @Test fun `refreshes each time the user logs in`() = runTest { /* fake.state: LoggedIn → LoggedOut → LoggedIn → FakeBootstrapRepository.refreshCalls == 2 */ }
    @Test fun `refresh failure shows a banner that the next login clears`() = runTest { /* refreshResult Failure(Offline) on first login → banner Res(error_offline); refreshResult Success; LoggedOut → LoggedIn → banner null */ }
}
```

- [ ] **Step 2: Run them and see them fail** — `./gradlew :composeApp:iosSimulatorArm64Test` → FAIL.

- [ ] **Step 3: Implement the wiring and platform entry points**

Debug-only cleartext on Android: `src/debug/res/xml/network_security_config.xml` permits cleartext for `10.0.2.2`, `localhost`, `127.0.0.1` only; `src/debug/AndroidManifest.xml` sets `android:networkSecurityConfig`. Release builds have no cleartext.

- [ ] **Step 4: Run all tests and all builds**

Run: `./gradlew :composeApp:iosSimulatorArm64Test :composeApp:testAndroidHostTest` → PASS. Then the three build commands from Task 1 Step 5 → all succeed. A `[KOIN-…]` error at this point is a real missing binding; fix it (often an `implementation` dependency that must be `api` or added to `composeApp`, per `ARCHITECTURE.md` §8).

- [ ] **Step 5: Run on Android (no API server exists yet)**

The owner chose app first, API later: no server speaks `/api/v1` yet, so a real login can't be completed in Phase 0. The logged-in flow is covered by the ViewModel and repository tests. Boot the `Pixel_10` emulator, `./gradlew :androidApp:installDebug`, launch `pk.paksoft.pos/.android.MainActivity`, and check with screenshots (`adb exec-out screencap -p`):
1. Shop picker shows Super Madina Solar Fan, BSF, Mummy & Me, Staging (dev), Local API (dev), in the purple theme.
2. **Local API (dev)** → log in with `test` / `test` → banner "No internet connection. Check your connection and try again." (nothing listens on port 8010).
3. **Change shop** → **Super Madina Solar Fan** → log in with `test` / `test` → banner "This shop isn't connected to the app yet." (the live site has no such route, so it answers 404 without the app's error envelope; nothing is written).
4. Empty fields + Log in → both field errors show.
5. Kill and relaunch → the app reopens on the login screen of the last chosen shop.
When an API exists (local backend plan, kept outside this repo at `PAKSOFTSolutionsPvtLtd/backend/plans/`), repeat with a real login: bottom bar, restart while logged in, offline refresh message, logout.

- [ ] **Step 6: Smoke run on iOS**

Build and install on the `iPhone 17 Pro` simulator (`xcodebuild … -derivedDataPath build/ios`, `xcrun simctl install booted …`, `xcrun simctl launch booted pk.paksoft.pos`), screenshot with `xcrun simctl io booted screenshot`. Expected: the shop picker in the PAKSOFT POS theme. Report exactly what was checked on iOS.

- [ ] **Step 7: Update docs**

`README.md` Status: Phase 0 app complete (what works, what was checked on each platform). `ARCHITECTURE.md`: correct anything Phase 0 changed (exact Android host-test task name, AppError shape, auth header plugin, `core:testing` contents).

- [ ] **Step 8: Commit**

`feat(app): Koin start, session gate and bottom navigation; Phase 0 complete`
