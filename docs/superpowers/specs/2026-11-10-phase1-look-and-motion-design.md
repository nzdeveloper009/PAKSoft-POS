# Phase 1: look and motion (design)

**Status:** approved by the owner on 10 November 2026 after reviewing a clickable preview (private artifact `https://claude.ai/artifact/E6GoHQ5h4UoDuiMc4cYMpU`). The preview's HTML is the visual reference; this document is the authority where they differ.

## Intent

The owner asked for a UI that is "good, attractive and not simple", follows Material 3, has a ripple and a spring on every press, smooth screen transitions, shared-element transitions where they fit, and bolder animation where it adds delight. Colours must not look like a generic AI-made app; the owner left the palette to us.

The look is built from the shop world: green for money coming in (and the ledger, the *khata*), marigold for the one action that takes money, and paper-like surfaces. Every shop gets its own colour and shape so a cashier who works in two shops always knows which one they are selling in.

## Goals

- Replace the Phase 0 lavender (SalePro's template purple `#7C5CC4`) with the palette below, light and dark.
- Material 3 Expressive throughout: shape-morphing buttons, the expressive motion scheme, the morphing loading indicator.
- One motion system (`PosMotion`) every screen uses; no hard-coded durations or easings in features.
- Shared-element and container transitions for the moments listed under "Motion moments".
- A **Demo shop** in debug builds so every screen can be run end to end before the `/api/v1` server exists.
- Re-skin the Phase 0 screens (shop picker, login, home shell, More, placeholders) in the new style.

## Non-goals (this step)

- Dashboard, POS, Sales and the other Phase 1 screens. They get their own plans and reuse everything built here. Their motion moments are recorded below so the components are built for them.
- Urdu, tablets beyond the existing rail, widgets, custom fonts beyond Nunito.

## Anti-goals

- Purple or blue-to-purple gradients, glassmorphism, neon on black, glow effects.
- Animation that delays work: no transition longer than about 500 ms on a path a cashier repeats, and nothing that blocks input after a tap.
- Literal colours, durations or easings in feature modules.

## Colour

All text pairs pass WCAG AA (4.5:1). Values are final; contrast ratios were computed and appear in the preview.

### Light scheme (Material 3 roles)

| Role | Hex | Role | Hex |
|---|---|---|---|
| primary | `#0B6B4F` | onPrimary | `#FFFFFF` |
| primaryContainer | `#B6EFD4` | onPrimaryContainer | `#002117` |
| secondary | `#4C6359` | onSecondary | `#FFFFFF` |
| secondaryContainer | `#CEE9DB` | onSecondaryContainer | `#082018` |
| tertiary | `#7D5700` | onTertiary | `#FFFFFF` |
| tertiaryContainer | `#FFC55C` | onTertiaryContainer | `#281800` |
| error | `#BA1A1A` | onError | `#FFFFFF` |
| errorContainer | `#FFDAD6` | onErrorContainer | `#410002` |
| background, surface | `#F6F7F2` | onBackground, onSurface | `#191C1A` |
| surfaceVariant | `#DCE5DE` | onSurfaceVariant | `#404943` |
| surfaceContainerLowest | `#FFFFFF` | surfaceContainerLow | `#EEF2EC` |
| surfaceContainer | `#E8ECE6` | surfaceContainerHigh | `#E1E6E0` |
| surfaceContainerHighest | `#DBE0DA` | surfaceDim / surfaceBright | `#D7DBD5` / `#F6F7F2` |
| outline | `#707973` | outlineVariant | `#BFC9C2` |
| inverseSurface | `#2E312F` | inverseOnSurface | `#EFF1EC` |
| inversePrimary | `#7EDAB3` | scrim | `#000000` |

### Dark scheme

| Role | Hex | Role | Hex |
|---|---|---|---|
| primary | `#7EDAB3` | onPrimary | `#003827` |
| primaryContainer | `#005139` | onPrimaryContainer | `#9AF6CE` |
| secondary | `#B3CCBF` | onSecondary | `#1E352C` |
| secondaryContainer | `#34493F` | onSecondaryContainer | `#CEE9DB` |
| tertiary | `#F2B13E` | onTertiary | `#2A1A00` |
| tertiaryContainer | `#F2B13E` (kept bright on purpose) | onTertiaryContainer | `#2A1A00` |
| error | `#FFB4AB` | onError | `#690005` |
| errorContainer | `#93000A` | onErrorContainer | `#FFDAD6` |
| background, surface | `#0F1412` | onBackground, onSurface | `#DFE4DF` |
| surfaceVariant | `#404943` | onSurfaceVariant | `#BFC9C2` |
| surfaceContainerLowest | `#0A0F0D` | surfaceContainerLow | `#171D1A` |
| surfaceContainer | `#1B211E` | surfaceContainerHigh | `#252B28` |
| surfaceContainerHighest | `#303633` | surfaceDim / surfaceBright | `#0F1412` / `#353A37` |
| outline | `#89938C` | outlineVariant | `#3F4943` |
| inverseSurface | `#DFE4DF` | inverseOnSurface | `#2E312F` |
| inversePrimary | `#0B6B4F` | scrim | `#000000` |

### Extended colours (`PosTheme.colors`)

Replace the Phase 0 `PosExtendedColors` (web payment colours) with:

| Name | Light | Dark | Use |
|---|---|---|---|
| due | `#9A6100` | `#FFB95A` | unpaid amounts, low stock text |
| dueContainer / onDueContainer | `#FFE2BA` / `#2C1700` | `#4A3000` / `#FFDDB5` | "Due" chips |
| paidContainer / onPaidContainer | `#B6EFD4` / `#002117` | `#005139` / `#9AF6CE` | "Paid" chips |
| chartIn | = primary | = primary | money received |
| chartOut | = tertiary | = tertiary | money paid out |

Errors use `colorScheme.error*`; success uses primary. Any feature still using a removed Phase 0 field moves to these or to scheme roles.

### Shop identities (`ShopBrand`)

| Shop id | Colour light / on | Colour dark / on | Container light / on | Container dark / on | Shape (`MaterialShapes`) | Initials |
|---|---|---|---|---|---|---|
| `smf` | `#E8890C` / `#2A1600` | `#FFB45C` / `#2A1600` | `#FFDDB5` / `#2A1600` | `#4A2C00` / `#FFDDB5` | `Sunny` | SM |
| `bsf` | `#2F5DA8` / `#FFFFFF` | `#A9C4FF` / `#00214F` | `#D8E2FF` / `#001A41` | `#1B3A70` / `#D8E2FF` | `Cookie9Sided` | BS |
| `mummyandme` | `#C2436E` / `#FFFFFF` | `#FFB1C8` / `#4A0025` | `#FFD9E2` / `#3E0020` | `#5E1133` / `#FFD9E2` | `Flower` | M& |
| `demo` | `#0B6B4F` / `#FFFFFF` | `#7EDAB3` / `#003827` | `#B6EFD4` / `#002117` | `#005139` / `#9AF6CE` | `Clover4Leaf` | DM |
| `staging`, `local`, any other | `#5D6670` / `#FFFFFF` | `#AEB8C2` / `#1A2128` | `#DDE3EA` / `#161C22` | `#3A434C` / `#DDE3EA` | `Square` | first two letters, upper case |

A shop's identity appears on its shop-picker card, the login header and the signed-in header (More now, Dashboard later). Buttons, charts and money stay ledger green in every shop.

## Type

Nunito variable (already bundled), weights 400–900 declared. Large numbers and screen titles use 800–900. Every money style uses tabular figures (`fontFeatureSettings = "tnum"`).

| Style | Size / line | Weight |
|---|---|---|
| displaySmall | 36 / 44 | 900 (money heroes) |
| headlineMedium | 28 / 36 | 900 (screen titles) |
| headlineSmall | 24 / 32 | 800 |
| titleLarge | 22 / 28 | 800 |
| titleMedium | 16 / 24 | 800 |
| titleSmall | 14 / 20 | 700 |
| bodyLarge / Medium / Small | 16/24, 14/20, 12/16 | 500 |
| labelLarge / Medium / Small | 15/20, 13/16, 11/16 | 700 |

Other M3 styles keep their default sizes in Nunito. `PosTheme.typography` adds `moneyLarge` (displaySmall + tnum), `moneyMedium` (titleLarge + tnum), `moneySmall` (titleMedium + tnum).

## Shape

Material 3 Expressive scale: extraSmall 8 dp, small 12, medium 16, large 24, extraLarge 28. Cards 24–28, product tiles 24, sheets 28 top corners, chips 12, the FAB 16. Buttons are fully round at rest and square off on press (`ButtonDefaults.shapes()`).

## Motion

### Tokens (`PosMotion`)

| Token | Spring (damping ratio, stiffness) | Use |
|---|---|---|
| `fastSpatial` | 0.6, 800 | press release, chips, nav indicator, FAB in and out |
| `spatial` | 0.8, 380 | screens, sheets, shared elements, cards entering, grid reflow |
| `slowSpatial` | 0.8, 200 | large moves the eye should follow |
| `fastEffects` | 1.0, 3800 | press-in squeeze, an outgoing screen fading |
| `effects` | 1.0, 1600 | fades and colour changes |
| `slowEffects` | 1.0, 800 | slow fades, a check mark drawing |
| `bouncy` | 0.45, 500 | the cart badge and the sale-complete badge only |
| `countUp` | 1.0, 120 | numbers counting up |

The first six equal Material 3 Expressive's `MotionScheme.expressive()`; the theme passes that motion scheme to `MaterialExpressiveTheme` so M3 components use it too. Entrances stagger children by 40–60 ms.

**Reduced motion.** `PosMotion` reads the platform setting (Android: animator duration scale 0, "Remove animations"; iOS: Reduce Motion). When on, spatial specs become `snap()`, effects become a 120 ms tween, and decorative motion (count-up, confetti, flying items, ambient rotation) is skipped so the final state shows at once.

### Interaction

- Every tappable surface: Material ripple (state layer), plus a scale to 0.95 on press (`fastEffects`) and back on release (`fastSpatial`). Large cards use 0.97, icon buttons 0.88.
- Buttons: `ButtonDefaults.shapes()` morph (round → squarer) on press.
- A button that starts work (Log in, later Complete sale) shrinks into a 56 dp circle with the M3 Expressive `LoadingIndicator` inside, and restores when the work ends.
- Filter chips: the check icon grows in on selection (`fastSpatial`).
- Haptics: `LocalHapticFeedback`, type `Confirm`, on add-to-cart and on sale complete (Phase 1 POS).

### Motion moments

| Moment | Animation | Built in |
|---|---|---|
| Shop picker → login | Shared element: avatar and name travel; the card's bounds grow into the login header; the form staggers in | this step |
| Login → home | Fade through (outgoing fades out, incoming scales 0.94 → 1) | this step |
| Log out, change shop | Shared axis back (slide 40 dp + fade) | this step |
| Push / pop inside home | Shared axis X with `spatial`; predictive back scales the leaving screen down to 0.9 and rounds its corners as the user swipes | this step |
| Tab switch | Fade through; the M3 nav indicator animates | this step |
| Entering a screen | Content sections rise 18 dp and fade in, staggered | this step |
| Dashboard figures | Count up; chart bars grow staggered | Dashboard |
| New sale FAB → POS | Container transform (`sharedBounds`) from the FAB into the POS screen, and back | Dashboard + POS |
| Add to cart | Item flies on an arc into the cart button; badge bumps (`bouncy`); total rolls like an odometer; haptic | POS |
| Category filter | Grid items animate to new positions (`animateItem`) | POS |
| Payment | Bottom sheet springs up; the selected payment method rounds out (M3 connected button group) | POS |
| Sale complete | Badge springs in, the check draws itself, shape confetti, the receipt rises | POS |
| Lists loading | Skeleton shimmer, then rows fade in | each list screen |

## Demo shop

A shop that exists only in debug builds and answers the API contract from inside the app, so the app runs end to end with no server.

- `Shop("demo", "Demo shop", "https://demo.paksoft.invalid/api/v1", isDevelopment = true)`, listed first among the dev shops. `.invalid` is reserved and never resolves, so a demo request can never reach a real host.
- In debug builds the `HttpClient` engine is wrapped: a request whose host is `demo.paksoft.invalid` is answered by an in-app `DemoServer`; every other request goes to the platform engine unchanged. Release builds use the platform engine directly.
- `DemoServer` returns the exact JSON shapes of `docs/API.md` (envelopes, errors, status codes), so DTOs, `ApiCaller`, repositories and Room run as they will against the real server.
- Login accepts any non-empty username and password and returns a demo user with every permission and the ledger feature on, so every tab shows. Token: `demo-token` (not a secret). Logout returns 204. `me` and `bootstrap` return the same demo user and sample settings.
- Sample data is invented: no real customer, supplier or product from any shop. The catalog is a solar shop's.
- The login screen shows "Demo shop: any username and password work." under the form when the demo shop is active.
- Later plans add each new endpoint to `DemoServer` together with its screen.

## Technical decisions (ours)

- **Material 3 version:** `org.jetbrains.compose.material3:material3` moves from 1.9.0 to `1.12.0-alpha03`, the build made for Compose Multiplatform 1.12 (it depends on Compose 1.12.0-beta01; our 1.12.2 satisfies it). The stable 1.9.0 keeps the Expressive APIs internal. `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` appears only in `core:designsystem`; features call our wrappers.
- **Icons:** Material Icons Rounded from `material-icons-core` (already a dependency), plus a few hand-added vectors in `PosIcons` for icons the core set lacks (receipt, ledger book, barcode scanner, eye). Path data is Material's (Apache 2.0). No new icon dependency. Features take icons from `PosIcons`.
- **Shapes:** `MaterialShapes` polygons drawn with `toShape()` (material3) for shop avatars and decorative shapes.
- **Shared elements:** `SharedTransitionLayout` at the app root. `core:designsystem` exposes `LocalSharedTransitionScope` and `LocalPosAnimatedVisibilityScope` plus `Modifier.posSharedElement(key)` and `Modifier.posSharedBounds(key)`, which do nothing when a scope is missing (previews) or motion is reduced.
- **Navigation transitions:** Navigation 3 `NavDisplay` `transitionSpec` / `popTransitionSpec` / `predictivePopTransitionSpec` use the shared-axis specs; top-level tab entries override them with fade through through entry metadata.
- **Session gate:** `App()` animates between session states with `AnimatedContent` inside the `SharedTransitionLayout`.

## Calls made after the builder check

- **Material 3 alpha:** the upgrade goes ahead. The first step of the design-system work resolves `1.12.0-alpha03` on Android and both iOS targets and confirms `MaterialExpressiveTheme`, `MotionScheme.expressive()`, `LoadingIndicator`, `ButtonDefaults.shapes()`, `MaterialShapes` and `toShape()` are public. If one is missing, we build our own equivalent in `core:designsystem` (springs are already ours; a missing indicator becomes a morphing-polygon composable) and keep the rest.
- **Predictive back:** the scale-and-round effect runs wherever `NavDisplay` delivers predictive back events (Android 14+). Elsewhere the normal pop transition runs. No custom gesture code.
- **Session gate and shared elements:** the shop picker and login are session-gate states in `App()` (not `NavDisplay` entries), so the gate's `AnimatedContent` supplies `LocalPosAnimatedVisibilityScope` for the picker → login shared elements. Inside the signed-in home, `NavDisplay`'s own animated scope is supplied to its entries for later shared elements.
- **Demo shop switch:** a runtime switch on `AppConfig.isDebug` (iOS has no debug-only source set). `DemoServer` code ships in release binaries but is never wired and the demo shop is never listed there; it holds only invented sample data. `Shop` keeps its current fields (`id`, `displayName`, `apiBaseUrl`, `isDevelopment`); `ARCHITECTURE.md`'s older `webBaseUrl` mention is corrected.
- **Demo endpoints in this step:** only `auth/login`, `auth/logout`, `me` and `bootstrap`. No new Room data.
- **Reduced motion:** an `expect`/`actual` reader in `core:designsystem`, read when the theme composes and again on each app resume.
- **Font:** keep the variable Nunito; weights 800 and 900 come from the weight axis as 400–700 already do on both platforms. Checked on iOS in the design-system step.

## Left to the builder

Exact composable names beyond those above, file layout inside `core:designsystem`, preview content, and how the stagger helper is shaped, as long as features never hard-code motion values.
