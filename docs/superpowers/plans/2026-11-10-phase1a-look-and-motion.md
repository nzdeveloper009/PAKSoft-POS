# Phase 1a: Look, Motion and Demo Shop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the app its approved colours, type, shapes and spring motion, re-skin the Phase 0 screens with shared-element transitions, and add a debug-only Demo shop so the app runs end to end without a server.

**Architecture:** Everything visual lives in `core:designsystem` (theme, `PosMotion`, components, shared-transition helpers); features and `composeApp` only call it. The Demo shop is an in-app `DemoServer` behind a debug-only `HttpClientEngine` wrapper in `core:network`, answering the `docs/API.md` contract so the whole data stack runs unchanged.

**Tech Stack:** Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 `1.12.0-alpha03` (Expressive), Navigation 3 1.2.0, Ktor 3.6.1, Koin 4.2.2 Annotations + Compiler Plugin.

**Spec:** `docs/superpowers/specs/2026-11-10-phase1-look-and-motion-design.md` (all colour, type, shape, motion and shop-identity values come from it verbatim).

## Global Constraints

- Every Gradle command is prefixed `JAVA_HOME=$(/usr/libexec/java_home -v 17)`. AGP stays at exactly 9.4.0.
- No new dependencies, modules or top-level destinations. The only version change is `compose-material3 = "1.12.0-alpha03"` (Task 2).
- `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` appears only in `core:designsystem`.
- No literal colours, durations, easings or spring values outside `core:designsystem`. Motion goes through `PosTheme.motion`.
- All user-visible text in the owning module's `composeResources/values/strings.xml`; apostrophes written plainly.
- Follow `CLAUDE.md` (Koin triad, no `!!`, `XxxRoute` + `XxxScreen` + `@Preview`, `collectAsStateWithLifecycle`, Money is `Long` paisa).
- Never contact a production shop. Manual checks use the Demo shop or the Local API (unreachable is fine).
- One commit per task (fix rounds may add commits); push to `origin main` only after the task review passes. Commit messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Done = `./gradlew allTests` green, `:androidApp:assembleDebug` and `:composeApp:linkDebugFrameworkIosSimulatorArm64` succeed, and UI tasks are looked at on the Android emulator (light and dark) with screenshots saved to `/Users/syed/AndroidStudioProjects/PAKSOFTSolutionsPvtLtd/phase1-review/screens/` (outside the repo).

## Review Focus

1. **Reduced motion on:** every animated path must still reach its final visible state (no content stuck at alpha 0, no button stuck shrunk). Test: `PosMotionTest` (Task 2) and `EnterStaggerTest`-style pure checks (Task 3).
2. **A tap during a transition:** tapping a shop twice, or Log in twice, must not navigate twice or crash. Test: `LoginViewModelTest` "submit while submitting is ignored" (Task 5 confirms it still exists and passes).
3. **Dark mode:** no component may use a light-only colour. Test: `ContrastTest` covers both schemes and every shop brand (Task 2).
4. **Demo isolation:** the engine wrapper must never answer a non-demo host, and release builds must not list the Demo shop. Tests: `DemoAwareEngineTest`, `ShopsTest` (Task 1).
5. **Previews and restored state without scopes:** shared-element modifiers must be no-ops when no `SharedTransitionScope` or animated scope is present. Test: `sharedTransitionEnabled` pure-function test (Task 4).

---

### Task 1: Demo shop

**Files:**
- Modify: `core/model/src/commonMain/kotlin/pk/paksoft/pos/core/model/Shop.kt`
- Create: `core/network/src/commonMain/kotlin/pk/paksoft/pos/core/network/demo/DemoServer.kt`
- Create: `core/network/src/commonMain/kotlin/pk/paksoft/pos/core/network/demo/DemoFixtures.kt`
- Create: `core/network/src/commonMain/kotlin/pk/paksoft/pos/core/network/demo/DemoAwareEngine.kt`
- Modify: `core/network/src/commonMain/kotlin/pk/paksoft/pos/core/network/PosHttpClient.kt`
- Modify: `feature/auth/.../login/LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt`, `feature/auth/src/commonMain/composeResources/values/strings.xml`
- Test: `core/model/src/commonTest/.../ShopsTest.kt`, `core/network/src/commonTest/.../demo/DemoServerTest.kt`, `core/network/src/commonTest/.../demo/DemoAwareEngineTest.kt`, `feature/auth/src/commonTest/.../LoginViewModelTest.kt`
- Docs: `README.md` (dev shops), `docs/ARCHITECTURE.md` (network section: the demo engine; fix the stale `webBaseUrl` mention of `Shop`)

**Interfaces:**
- Produces: `Shops.DEMO_ID = "demo"`, `Shops.DEMO_HOST = "demo.paksoft.invalid"`, `Shops.demo: Shop` = `Shop("demo", "Demo shop", "https://demo.paksoft.invalid/api/v1", isDevelopment = true)`. `Shops.forBuild(isDebug = true, …)` returns `production + demo + staging + local` in that order.
- Produces: `internal class DemoServer { fun respond(method: HttpMethod, encodedPath: String): DemoResponse }`, `internal data class DemoResponse(val status: HttpStatusCode, val body: String?)`.
- Produces: `internal class DemoAwareEngine(real: HttpClientEngine, server: DemoServer) : HttpClientEngineBase`. `createPosHttpClient(...)` keeps its signature and wraps the engine only when `config.isDebug`.
- Produces: `LoginUiState.isDemoShop: Boolean`.

- [ ] **Step 1: Write the failing tests**

`ShopsTest`: `` `debug builds list the demo shop first among dev shops`() `` asserts `Shops.forBuild(true, "http://10.0.2.2/api/v1").map { it.id } == listOf("smf", "bsf", "mummyandme", "demo", "staging", "local")`; `` `release builds never list the demo shop`() `` asserts `Shops.forBuild(false, "x").none { it.id == "demo" }`.

`DemoServerTest` (decode with `PosJson` and the existing DTOs):
- `` `login answers a session envelope`() ``: `respond(Post, "/api/v1/auth/login")` → status 200, body decodes as `DataEnvelope<SessionDto>`, token `"demo-token"`, permissions contain `Permissions.SALES_ADD` and `Permissions.SALES_INDEX`.
- `` `me and bootstrap decode`() ``: `Get /api/v1/me` → `SessionDto`; `Get /api/v1/bootstrap` → `BootstrapDto` whose settings enable the ledger feature.
- `` `logout answers 204 with no body`() ``.
- `` `unknown path answers the not_found envelope`() ``: `Get /api/v1/nope` → 404, body decodes as `ErrorEnvelopeDto` with code `"not_found"`.

`DemoAwareEngineTest` (build a client with `createPosHttpClient` over a `MockEngine` that records requests, `AppConfig(isDebug = true, …)`):
- `` `demo host is answered in-app and never reaches the real engine`() ``: `GET https://demo.paksoft.invalid/api/v1/me` → 200 and the MockEngine saw 0 requests.
- `` `other hosts pass through unchanged`() ``: `GET https://example.test/api/v1/me` reaches the MockEngine once with the same URL and the `Authorization`/`X-Device-Id` headers set by `PosHeaders`.
- `` `release config does not wrap the engine`() ``: with `isDebug = false`, a demo-host request reaches the MockEngine.
- `` `KtorAuthApi logs in against the demo shop end to end`() ``: active shop = `Shops.demo`, `login(...)` returns `AppResult.Success` with token `"demo-token"`.

`LoginViewModelTest`: `` `demo shop shows the demo hint`() `` (state `isDemoShop == true` when the logged-out shop id is `"demo"`, false for `"smf"`).

- [ ] **Step 2: Run them and see them fail**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:model:testAndroidHostTest :core:network:testAndroidHostTest :feature:auth:testAndroidHostTest`
Expected: compilation failures for the missing symbols.

- [ ] **Step 3: Implement**

- `DemoFixtures`: JSON strings shaped exactly like the `docs/API.md` examples for login/me (session) and bootstrap, with invented demo values: user "Ayesha (demo)", username `demo`, role id 1, every permission string defined in `Permissions`, `features.ledger = true`, one warehouse "Main store", one biller "Demo Solar Traders", currency PKR. No real names, phones or addresses from any shop.
- `DemoServer.respond`: routes on method + path relative to `/api/v1/`: `POST auth/login` → 200 session, `POST auth/logout` → 204, `GET me` → 200 session, `GET bootstrap` → 200 bootstrap, anything else → 404 `{"error":{"code":"not_found","message":"Not found."}}`.
- `DemoAwareEngine` (the only non-obvious code; `execute` and `callContext` are `@InternalAPI` in Ktor 3, so opt in here only):

```kotlin
@OptIn(InternalAPI::class)
internal class DemoAwareEngine(
    private val real: HttpClientEngine,
    private val server: DemoServer,
) : HttpClientEngineBase("pos-demo-aware") {
    override val config: HttpClientEngineConfig get() = real.config
    override val supportedCapabilities: Set<HttpClientEngineCapability<*>> get() = real.supportedCapabilities

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        if (data.url.host != Shops.DEMO_HOST) return real.execute(data)
        val answer = server.respond(data.method, data.url.encodedPath)
        return HttpResponseData(
            statusCode = answer.status,
            requestTime = GMTDate(),
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(answer.body.orEmpty().encodeToByteArray()),
            callContext = callContext(),
        )
    }

    override fun close() {
        super.close()
        real.close()
    }
}
```

  If Ktor 3.6.1 names differ, adapt to the equivalent public-or-`@InternalAPI` API and keep the behavior; if delegation to the real engine cannot work, report BLOCKED with the error rather than switching approach.
- `createPosHttpClient`: `HttpClient(if (config.isDebug) DemoAwareEngine(engine, DemoServer()) else engine) { … }`.
- Login: `LoginUiState.isDemoShop = loggedOut.shop.id == Shops.DEMO_ID`; `LoginScreen` shows string `login_demo_hint` = "Demo shop: any username and password work." under the form when true.

- [ ] **Step 4: Run tests, builds and a manual check**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. Then on the Pixel emulator (debug build): pick Demo shop → log in with any text → the signed-in home shows all five tabs; More → Log out returns to login; pick Local API → Log in shows the existing "No internet connection" banner (proves the real engine path still works). Save screenshots `t1-demo-login.png`, `t1-demo-home.png`.

- [ ] **Step 5: Commit**

```bash
git add -A core/model core/network feature/auth README.md docs/ARCHITECTURE.md
git commit -m "feat(network): add the debug-only Demo shop served in-app"
```

---

### Task 2: Theme v2 with Material 3 Expressive

**Files:**
- Modify: `gradle/libs.versions.toml` (`compose-material3 = "1.12.0-alpha03"`)
- Rewrite: `core/designsystem/src/commonMain/kotlin/pk/paksoft/pos/core/designsystem/theme/PosColors.kt`, `PosTypography.kt`, `PosTheme.kt`
- Create: `theme/ShopBrand.kt`, `theme/PosShapes.kt`, `theme/PosMotion.kt`, `theme/ReduceMotion.kt` (`expect`), `src/androidMain/.../theme/ReduceMotion.android.kt`, `src/iosMain/.../theme/ReduceMotion.ios.kt`
- Modify: any file that used a removed `PosExtendedColors` field (find with `grep -rn "PosTheme.colors" --include='*.kt' .`)
- Test: `core/designsystem/src/commonTest/.../theme/ContrastTest.kt`, `ShopBrandTest.kt`, `PosMotionTest.kt`
- Docs: `docs/ARCHITECTURE.md` design-system section (tokens, `PosMotion`, `ShopBrand`); `CLAUDE.md` Compose conventions gain: "Motion only from `PosTheme.motion`; shared elements only through `posSharedElement`/`posSharedBounds`."

**Interfaces:**
- Produces (all `public`, package `pk.paksoft.pos.core.designsystem.theme`):
  - `val PosLightColorScheme: ColorScheme`, `val PosDarkColorScheme: ColorScheme` with every role from the spec tables.
  - `data class PosExtendedColors(val due: Color, val dueContainer: Color, val onDueContainer: Color, val paidContainer: Color, val onPaidContainer: Color, val chartIn: Color, val chartOut: Color)`; `PosLightExtendedColors`, `PosDarkExtendedColors`.
  - `enum class ShopShape { SUNNY, COOKIE_9, FLOWER, CLOVER_4, SQUARE }` and `fun ShopShape.polygon(): RoundedPolygon` (maps to `MaterialShapes.Sunny`, `Cookie9Sided`, `Flower`, `Clover4Leaf`, `Square`).
  - `data class ShopBrandColors(val color: Color, val onColor: Color, val container: Color, val onContainer: Color)`; `data class ShopBrand(val initials: String, val shape: ShopShape, val light: ShopBrandColors, val dark: ShopBrandColors)` with `companion fun forShop(id: String, displayName: String): ShopBrand`.
  - `class PosMotion(val reduced: Boolean)` with `fun <T> fastSpatial(): FiniteAnimationSpec<T>`, `spatial()`, `slowSpatial()`, `fastEffects()`, `effects()`, `slowEffects()`, `bouncy()`, `countUp()`, and `val staggerStepMillis: Int = 50`, `val enterOffset: Dp = 18.dp`.
  - `@Composable expect fun rememberReduceMotion(): Boolean`.
  - `data class PosTypographyExtras(val moneyLarge: TextStyle, val moneyMedium: TextStyle, val moneySmall: TextStyle)`.
  - `object PosTheme { val colors; val typography: PosTypographyExtras; val motion: PosMotion; val isDark: Boolean }` (all `@Composable @ReadOnlyComposable` getters) and `@Composable fun ShopBrand.colors(): ShopBrandColors` (picks `dark` when `PosTheme.isDark`).
  - `PosTheme(darkTheme, content)` keeps its signature and now wraps `MaterialExpressiveTheme(colorScheme, motionScheme = MotionScheme.expressive(), shapes = PosShapes, typography = posTypography())`.

- [ ] **Step 1: Bump Material 3 and confirm the Expressive API surface**

Set `compose-material3 = "1.12.0-alpha03"`. Run `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:designsystem:compileKotlinIosSimulatorArm64 :core:designsystem:compileAndroidMain` (use the module's actual Android compile task name if different). Then confirm, in the resolved sources, that `MaterialExpressiveTheme`, `MotionScheme.expressive()`, `LoadingIndicator`, `ButtonDefaults.shapes()`, `MaterialShapes` (with `Sunny`, `Cookie9Sided`, `Flower`, `Clover4Leaf`, `Square`) and `RoundedPolygon.toShape()` are public. Record the result in the report. A missing piece follows the spec's fallback (build our own in `core:designsystem`).

- [ ] **Step 2: Write the failing tests**

`ContrastTest` (implement WCAG relative luminance in the test file):
- `` `every light scheme text pair passes AA`() `` and `` `every dark scheme text pair passes AA`() ``: for each pair (primary/onPrimary, primaryContainer/onPrimaryContainer, secondary…, tertiary…, tertiaryContainer…, error…, errorContainer…, surface/onSurface, surface/onSurfaceVariant, surfaceContainerLow/onSurfaceVariant, inverseSurface/inverseOnSurface) assert ratio ≥ 4.5.
- `` `extended colours pass AA`() ``: dueContainer/onDueContainer, paidContainer/onPaidContainer, and `due` on `surface`, in both themes.
- `` `every shop brand passes AA in both themes`() ``: for ids `smf, bsf, mummyandme, demo, staging, local`, color/onColor and container/onContainer ≥ 4.5.

`ShopBrandTest`: `forShop("smf", …)` → shape `SUNNY`, initials `"SM"`, light color `Color(0xFFE8890C)`; `bsf` → `COOKIE_9`, `"BS"`; `mummyandme` → `FLOWER`, `"M&"`; `demo` → `CLOVER_4`, `"DM"`; `forShop("staging", "Staging (dev)")` → `SQUARE`, `"ST"`; `forShop("xyz", "Local API (dev)")` → `SQUARE`, `"LO"`.

`PosMotionTest`: `PosMotion(reduced = false).spatial<Float>()` is a `SpringSpec` with `dampingRatio == 0.8f` and `stiffness == 380f`; same check for every token against the spec table; `PosMotion(reduced = true).spatial<Float>()` is a `SnapSpec`; `PosMotion(reduced = true).effects<Float>()` is a `TweenSpec` with `durationMillis == 120`.

- [ ] **Step 3: Run them and see them fail**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:designsystem:testAndroidHostTest`
Expected: compilation failures.

- [ ] **Step 4: Implement**

Values verbatim from the spec. Typography: declare Nunito variable weights 400, 500, 600, 700, 800, 900 via `FontVariation.weight`; set the styles listed in the spec's Type table; money styles add `fontFeatureSettings = "tnum"`. `PosShapes = Shapes(extraSmall 8, small 12, medium 16, large 24, extraLarge 28)` (rounded corners, dp). `ReduceMotion.android.kt`: read `Settings.Global.ANIMATOR_DURATION_SCALE` (0 → reduced), re-read on `Lifecycle.Event.ON_RESUME`. `ReduceMotion.ios.kt`: `UIAccessibilityIsReduceMotionEnabled()`, re-read on resume. `PosTheme` provides `LocalPosMotion`, `LocalPosExtendedColors`, `LocalPosTypographyExtras`, `LocalPosIsDark`.

- [ ] **Step 5: Run tests, builds, and look at it**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. Launch on the Android emulator in light and dark (`adb shell cmd uimode night yes|no`): the existing screens now show green/paper colours, no lavender. Launch the iOS app on the simulator once and confirm the shop picker renders Nunito at the new weights. Save `t2-light.png`, `t2-dark.png`, `t2-ios.png`.

- [ ] **Step 6: Commit**

```bash
git add -A gradle/libs.versions.toml core/designsystem feature composeApp docs/ARCHITECTURE.md CLAUDE.md
git commit -m "feat(designsystem): ledger-green theme, shop brands and PosMotion on Material 3 Expressive"
```

---

### Task 3: Components

**Files (all under `core/designsystem/src/commonMain/kotlin/pk/paksoft/pos/core/designsystem/`):**
- Create: `component/PressScale.kt`, `component/ShopAvatar.kt`, `component/RollingText.kt`, `component/CountUp.kt`, `component/PosFilterChip.kt`, `component/Skeleton.kt`, `component/EnterStagger.kt`, `component/PosLoadingIndicator.kt`, `icon/PosIcons.kt`
- Rewrite: `component/PosButton.kt`, `ErrorBanner.kt`, `EmptyState.kt`, `LoadingState.kt`, `PosTextField.kt`, `PosTopBar.kt`
- Modify: callers of `PosOutlinedButton` (replace with `PosButton(style = PosButtonStyle.Outlined)`)
- Test: `core/designsystem/src/commonTest/.../component/RollingTextTest.kt`, `CountUpTest.kt`, `EnterStaggerTest.kt`

**Interfaces:**
- Consumes: `PosTheme.motion`, `ShopBrand`, `ShopShape.polygon()` (Task 2).
- Produces:
  - `fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.95f): Modifier` (scale down with `fastEffects` while pressed, back with `fastSpatial`; no-op when reduced).
  - `@Composable fun Modifier.posClickable(enabled: Boolean = true, pressedScale: Float = 0.97f, role: Role? = null, onClick: () -> Unit): Modifier` (ripple + `pressScale`, one shared `MutableInteractionSource`).
  - `enum class PosButtonStyle { Filled, Charge, Tonal, Outlined, Text }`; `@Composable fun PosButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, style: PosButtonStyle = PosButtonStyle.Filled, large: Boolean = false, enabled: Boolean = true, loading: Boolean = false, icon: ImageVector? = null)`. Charge uses tertiaryContainer/onTertiaryContainer. `shapes = ButtonDefaults.shapes()` (morph on press) plus `pressScale`. `large` = 56 dp height. `loading` animates the width to a 56 dp circle (`spatial`) showing `PosLoadingIndicator`, ignores clicks, and restores the width when it ends.
  - `@Composable fun PosLoadingIndicator(modifier: Modifier = Modifier, color: Color = LocalContentColor.current)` (M3 Expressive `LoadingIndicator`).
  - `@Composable fun ShopAvatar(brand: ShopBrand, size: Dp, modifier: Modifier = Modifier)` (polygon `toShape()` filled with `brand.colors().color`, initials in `onColor`, font size = size × 0.3, weight 900).
  - `@Composable fun RollingText(text: String, style: TextStyle, modifier: Modifier = Modifier)` (each digit rolls vertically to its new value with `spatial`, right-most digit first with an 18 ms stagger; non-digits swap instantly; reduced → plain text) and `internal fun rollingSlots(text: String): List<RollingSlot>` where `sealed interface RollingSlot { data class Digit(val value: Int); data class Fixed(val char: Char) }`.
  - `@Composable fun animateCountAsState(target: Long): State<Long>` (`countUp` spring; reduced → jumps) and `internal fun countAt(from: Long, to: Long, fraction: Float): Long`.
  - `@Composable fun PosFilterChip(selected: Boolean, onClick: () -> Unit, label: String, modifier: Modifier = Modifier)` (check icon grows in with `fastSpatial`, `posClickable` feel).
  - `fun Modifier.skeleton(visible: Boolean): Modifier` (shimmer over `surfaceContainerHigh`; static block when reduced) and `@Composable fun SkeletonBox(modifier: Modifier)`.
  - `@Composable fun Modifier.enterStagger(index: Int): Modifier` (on first composition: from alpha 0 and `enterOffset` below, delay `index × staggerStepMillis`, `spatial`; reduced → identity) and `internal fun staggerDelayMillis(index: Int, stepMillis: Int, reduced: Boolean): Int` (index clamped to 0..8; 0 when reduced).
  - `object PosIcons` exposing `ImageVector`s: `Home, Pos (shopping cart), Sales (receipt long), Ledger (menu book), More (menu), Back, Add, Check, Search, Scan, Refresh, Person, Visibility, VisibilityOff, Share, Logout, Store, ChevronRight, Error, CloudOff`. Use `Icons.Rounded.*` / `Icons.AutoMirrored.Rounded.*` from material-icons-core where they exist; hand-build the rest with `ImageVector.Builder` from Material's path data.
  - Restyled: `ErrorBanner` (errorContainer surface, 16 dp corners, tonal Retry), `EmptyState` (`Clover4Leaf` shape in secondaryContainer with an icon, title + body), `LoadingState` (`PosLoadingIndicator` centred), `PosTextField` (outlined, 16 dp corners), `PosTopBar` (title in headlineSmall/800).
  - Every new or restyled component has a `@Preview` in light and dark.

- [ ] **Step 1: Write the failing tests**

`RollingTextTest`: `rollingSlots("Rs 18,750")` == `[Fixed('R'), Fixed('s'), Fixed(' '), Digit(1), Digit(8), Fixed(','), Digit(7), Digit(5), Digit(0)]`; `rollingSlots("")` is empty.
`CountUpTest`: `countAt(0, 482350, 0f) == 0`, `countAt(0, 482350, 1f) == 482350`, `countAt(100, 50, 0.5f) == 75`, `countAt(0, 10, 1.08f) == 11` (overshoot passes through; the caller clamps nothing).
`EnterStaggerTest`: `staggerDelayMillis(3, 50, reduced = false) == 150`; `staggerDelayMillis(20, 50, false) == 400` (clamped at index 8); `staggerDelayMillis(3, 50, reduced = true) == 0`.

- [ ] **Step 2: Run them and see them fail**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:designsystem:testAndroidHostTest`
Expected: compilation failures.

- [ ] **Step 3: Implement the components listed under Interfaces.**

- [ ] **Step 4: Run tests and builds**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. Open the Android app (Demo shop) and confirm buttons squeeze and square off on press, the Log in button turns into the loading shape, and the error banner/empty state render in light and dark. Save `t3-press.png`, `t3-loading.png`, `t3-dark.png`.

- [ ] **Step 5: Commit**

```bash
git add -A core/designsystem feature composeApp
git commit -m "feat(designsystem): spring press, morphing buttons, shop avatars, rolling numbers and skeletons"
```

---

### Task 4: Transitions: session gate, shared elements, navigation

**Files:**
- Create: `core/designsystem/src/commonMain/kotlin/pk/paksoft/pos/core/designsystem/motion/SharedTransitions.kt`, `motion/PosTransitions.kt`
- Modify: `composeApp/src/commonMain/kotlin/pk/paksoft/pos/app/App.kt`, `app/main/MainScaffold.kt`, `app/main/TopLevelDestination.kt`, `app/main/ComingSoonScreen.kt`
- Create: `composeApp/src/commonMain/kotlin/pk/paksoft/pos/app/SessionTransitions.kt`
- Test: `core/designsystem/src/commonTest/.../motion/SharedTransitionsTest.kt`, `composeApp/src/commonTest/.../SessionTransitionsTest.kt` (create the source set dependency only if `composeApp` lacks one; if adding a test source set is not possible without a new dependency, put the pure function in `core:ui` and test it there)
- Docs: `docs/ARCHITECTURE.md` navigation section (transitions, metadata override for tabs, shared-element helpers)

**Interfaces:**
- Consumes: `PosTheme.motion` (Task 2), `PosIcons` (Task 3).
- Produces (designsystem, package `…designsystem.motion`):
  - `val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?>` (default null), `val LocalPosAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?>` (default null).
  - `@Composable fun Modifier.posSharedElement(key: Any): Modifier` and `@Composable fun Modifier.posSharedBounds(key: Any): Modifier` (use `spatial` bounds transform; no-op when `sharedTransitionEnabled(...)` is false).
  - `fun sharedTransitionEnabled(hasSharedScope: Boolean, hasVisibilityScope: Boolean, reduced: Boolean): Boolean`.
  - `object PosTransitions { fun sharedAxisForward(motion: PosMotion): ContentTransform; fun sharedAxisBack(motion: PosMotion): ContentTransform; fun fadeThrough(motion: PosMotion): ContentTransform }` (shared axis: slide 40 dp + fade, incoming `spatial`, outgoing `fastEffects`; fade through: outgoing fades with `fastEffects`, incoming fades and scales 0.94 → 1 with `spatial`).
  - Shared keys used by Task 5: `"shop-card-$id"`, `"shop-avatar-$id"`, `"shop-name-$id"` (expose as `fun shopCardKey(id: String)`, `shopAvatarKey`, `shopNameKey` in `SharedTransitions.kt`).
- Produces (composeApp): `enum class SessionScreen { Starting, ShopPicker, Login, Home }`, `fun sessionScreen(state: SessionState): SessionScreen`, `fun sessionTransition(from: SessionScreen, to: SessionScreen): SessionTransition` with `enum class SessionTransition { SharedForward, SharedBack, FadeThrough }`.

- [ ] **Step 1: Write the failing tests**

`SharedTransitionsTest`: `sharedTransitionEnabled(true, true, false)` is true; any `false` scope or `reduced = true` gives false.
`SessionTransitionsTest`: `sessionTransition(ShopPicker, Login) == SharedForward`; `(Login, ShopPicker) == SharedBack`; `(Login, Home) == FadeThrough`; `(Home, Login) == SharedBack`; `(Starting, X) == FadeThrough` for every X; `(Home, ShopPicker) == SharedBack`.

- [ ] **Step 2: Run them and see them fail** (`:core:designsystem:testAndroidHostTest` and the composeApp/core:ui test task).

- [ ] **Step 3: Implement**

- `App()`: `PosTheme { SharedTransitionLayout { AnimatedContent(targetState = state, contentKey = { sessionScreen(it) to (it as? SessionState.LoggedOut)?.shop?.id }, transitionSpec = { … from sessionTransition(...) via PosTransitions … }) { current -> CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout, LocalPosAnimatedVisibilityScope provides this@AnimatedContent) { …existing when(current)… } } } }`. Keep `SafeArea` and the `key(shop.id)` behaviour.
- `MainScaffold`: `NavDisplay(transitionSpec = { PosTransitions.sharedAxisForward(motion) }, popTransitionSpec = { PosTransitions.sharedAxisBack(motion) }, predictivePopTransitionSpec = { the back spec, with the leaving content scaled to 0.9 and clipped to 28 dp corners as progress grows })`. Top-level entries (`DashboardKey`, `PosKey`, `SalesKey`, `LedgerKey`, `MoreKey`) carry metadata that switches both specs to `fadeThrough`. Provide `LocalPosAnimatedVisibilityScope` from `LocalNavAnimatedContentScope` inside entries (via an entry decorator or a wrapper composable).
- Nav bar and rail: `NavigationBar`/`NavigationRail` with containerColor `surfaceContainer`, indicator `secondaryContainer`, icons from `PosIcons`, `labelMedium`; the selected icon gives a small `fastSpatial` scale bump (1 → 1.1 → 1).
- `ComingSoonScreen`: `EmptyState` with the tab's icon and the existing copy, content `enterStagger`.

- [ ] **Step 4: Run tests, builds and look at it**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. On the emulator with the Demo shop: login → home fades through; tabs fade through; More → Log out slides back; a back swipe from a tab previews the shrink (Android 14+ emulator). Save `t4-tabs.png`, `t4-predictive-back.png`.

- [ ] **Step 5: Commit**

```bash
git add -A core/designsystem composeApp core/ui docs/ARCHITECTURE.md
git commit -m "feat(app): spring transitions for the session gate, tabs, pushes and predictive back"
```

---

### Task 5: Re-skin shop picker, login and More

**Files:**
- Modify: `feature/auth/.../shop/ShopPickerUiState.kt`, `ShopPickerViewModel.kt`, `ShopPickerScreen.kt`; `feature/auth/.../login/LoginUiState.kt`, `LoginViewModel.kt`, `LoginScreen.kt`; `feature/settings/.../MoreUiState.kt`, `MoreViewModel.kt`, `MoreScreen.kt`; both modules' `strings.xml`
- Test: `feature/auth/src/commonTest/.../ShopPickerViewModelTest.kt`, `LoginViewModelTest.kt`, `feature/settings/src/commonTest/.../MoreViewModelTest.kt`

**Interfaces:**
- Consumes: `ShopBrand.forShop`, `ShopAvatar`, `PosButton`, `posClickable`, `enterStagger`, `posSharedElement`/`posSharedBounds` and the shop keys (Tasks 2–4).
- Produces: `ShopItem(id: String, name: String, host: String, isDevelopment: Boolean)`; `LoginUiState` gains `shopId: String`, `shopHost: String`; `MoreUiState` gains `shopId: String`, `shopHost: String` (if not present). `host` = the API base URL without scheme and without a trailing `/api/v1` (`https://pbsf.org.pk/smf/api/v1` → `pbsf.org.pk/smf`; `https://pos.mummyandme.com.pk/api/v1` → `pos.mummyandme.com.pk`), computed by `fun shopHost(apiBaseUrl: String): String` in `core:ui`.

- [ ] **Step 1: Write the failing tests**

`ShopPickerViewModelTest`: items carry `host` (`"pbsf.org.pk/smf"` for smf, `"pos.mummyandme.com.pk"` for mummyandme). A `core:ui` test for `shopHost` covers both forms plus `http://10.0.2.2:8000/api/v1` → `"10.0.2.2:8000"`.
`LoginViewModelTest`: state has `shopId` and `shopHost` of the logged-out shop; existing "submit while submitting is ignored" still passes (add it if missing: two `Submit` actions while the fake API suspends → one login call).
`MoreViewModelTest`: state has `shopId` and `shopHost`.

- [ ] **Step 2: Run them and see them fail.**

- [ ] **Step 3: Implement (look per the spec and the approved preview)**

- Shop picker: brand row (small `Cookie4Sided` mark + "PAKSOFT POS" in primary, labelLarge/900), title "Choose your shop" (headlineMedium), subtitle; cards on `surfaceContainerLow`, 28 dp corners, `ShopAvatar(52.dp)`, name (titleMedium), host (bodySmall, onSurfaceVariant), chevron, "Dev" badge for dev shops; `posClickable(pressedScale = 0.97f)`; cards `enterStagger(index)`; `posSharedBounds(shopCardKey(id))` on the card, `posSharedElement(shopAvatarKey(id))` on the avatar, `posSharedElement(shopNameKey(id))` on the name.
- Login: hero on the brand container colour with 32 dp bottom corners: back icon button ("Switch shop", `onContainer`), `ShopAvatar(84.dp)`, shop name (headlineSmall/800, `onContainer`), host; the same shared keys (bounds on the hero). Form below with `enterStagger`: login and password fields, `PosButton(large = true, loading = isSubmitting)`, demo hint (Task 1), banner via `ErrorBanner`.
- More: header card with `ShopAvatar(56.dp)` on the brand container, shop name, host, user name and role; actions as `posClickable` list cards (Refresh shop data, Switch shop) and a `PosButton(style = Outlined)` Log out; success and error messages keep their Phase 0 behaviour (`isMessageError` → `ErrorBanner`).

- [ ] **Step 4: Run tests, builds and look at it on both platforms**

Run: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64` and the `xcodebuild` command from `CLAUDE.md` (expect `** BUILD SUCCEEDED **`).
On the Android emulator, light and dark: shop picker; tap Super Madina Solar Fan and watch the avatar and name travel into the login header; Demo shop login → home; More. Turn on "Remove animations" (`adb shell settings put global animator_duration_scale 0`) and confirm every screen still shows its content, then restore it to 1. On the iOS simulator: shop picker → Demo shop → login → home. Save `t5-picker-light.png`, `t5-picker-dark.png`, `t5-login-smf.png`, `t5-login-demo.png`, `t5-more.png`, `t5-ios-home.png`.

- [ ] **Step 5: Commit**

```bash
git add -A feature core/ui
git commit -m "feat(auth,settings): re-skin shop picker, login and More with shop identities"
```

---

## After the last task

Update `README.md` status (Phase 1a done: new look, motion, Demo shop) in the final fix wave, after the whole-branch review.
