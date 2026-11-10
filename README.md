# PAKSOFT POS: Android & iOS app

A Compose Multiplatform app (Android + iOS) that brings the **SalePro point of sale and inventory system** of three shops (**Super Madina Solar Fan `smf`, BSF `bsf`, and Mummy & Me `mummyandme`**) to phones and tablets: same shops, same data, same rules as the website.

> **Scope:** the app is only for these three POS shops. The WordPress website at the root of `pbsf.org.pk` (Pakistan Blind Sports Federation) is **not** part of the app; it only shares the same hosting account. "PBSF" is just this project's folder name.

**Status (11 November 2026):** Phase 0 app complete, and **Phase 1a done**: the new look (ledger green and marigold, light and dark), spring motion on Material 3 Expressive, shop identities, transitions (shared element shop picker → login, fade-through tabs, predictive back) and the Demo shop in debug builds. No API server exists yet (app first, API later). Next: the Phase 1 screens, built against the Demo shop, and the `/api/v1` backend.

- **Works:** shop picker (three shops, plus Demo shop, Staging and Local API in debug builds), each shop wearing its own identity (avatar and colours on the picker card, the login header and More); login with field checks and error banners; the session gate (shop picker → login → home) with spring transitions: the tapped shop card grows into the login header, the header's back arrow and system Back return to the picker, logging in fades through to home, logging out and switching shop go back on the shared axis; a saved session that survives restarts and works offline; bottom navigation on phones and a navigation rail on wide screens (Dashboard · POS · Sales · Ledger · More, shown by permission; all but More are placeholders) with fade-through tab changes and predictive back; the More screen (refresh shop data, switch shop, log out); the shop-data refresh banner with Retry; light and dark themes and the system's reduce-motion setting (every animation lands at once); the Demo shop (debug builds only: any username and password, invented sample data, no server).
- **Checked on Android** (Pixel 10 emulator): all Phase 1a screens and transitions on the Demo shop, in light, dark and reduced motion: shop picker, login (field errors, loading button), home and tabs, More, refresh, switch shop, logout, the shared transition picker → login and back, predictive back, system Back from the login to the picker and from the picker out of the app, and taps ignored on a screen while it animates out. Local API traffic through the debug engine wrapper reached a local server (its answer showed in the login's error banner). From Phase 0: login errors against an unreachable Local API and against a live shop that has no API yet ("This shop isn't connected to the app yet."); relaunch reopening the last shop's login; offline restart with the cached session, Retry and the rail in landscape, against a throwaway local mock of the API examples.
- **Checked on iOS** (iPhone 17 Pro simulator): the app builds and shows the shop picker in the new look, light and dark, and the Demo shop's login (opened by seeding the saved shop; there is no tap tool on this Mac).
- **Not verified yet:** a real login on either platform and the iOS Keychain token store (no API server exists, so no real token has been written); on iOS, anything past the Demo login (tapping through to home, and the shop picker → login shared transition); iOS real-host traffic through the debug engine wrapper (Darwin). The real login and the Keychain are the first checks once the API is up.
- **Tests:** unit tests on every module, run as `iosSimulatorArm64Test` and `testAndroidHostTest`; Koin's graph is checked at compile time.

| | |
|---|---|
| Web system (reference) | `../pbsf.org.pk/smf` (Laravel 6.20, PHP 7.4, MySQL); see `docs/DOMAIN.md` |
| Live site | https://pbsf.org.pk/smf (Super Madina Solar Fan) |
| This app | `PBSF/` (this folder) |
| Shareable overview | https://claude.ai/artifact/CjximFVjgVcBfLWYGFpN8z (private until shared from its Share menu) |

---

## The shops

One web codebase is deployed three times, each with its own database. The app lets the user pick a shop, then log in with their existing web username (or email) and password.

| Shop | Web | API (to be built) |
|---|---|---|
| Super Madina Solar Fan | https://pbsf.org.pk/smf | `/smf/api/v1` |
| BSF | https://pbsf.org.pk/bsf | `/bsf/api/v1` |
| Mummy & Me | https://pos.mummyandme.com.pk | `/api/v1` (needs its code aligned with `smf` first) |

## Documentation

Read in this order:

| File | What it answers |
|---|---|
| `README.md` | What this is, how to build it, roadmap, open decisions |
| [`CLAUDE.md`](CLAUDE.md) | Rules and recipes for writing code here (for Claude Code and humans) |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Modules, layers, DI, navigation, data flow, design system, versions |
| [`docs/DOMAIN.md`](docs/DOMAIN.md) | How the existing POS behaves: roles, statuses, pricing and cart math, sales, receipts, ledger, tables |
| [`docs/API.md`](docs/API.md) | The JSON API contract (requests, responses, errors) and how to build it in Laravel |

## Why there are two builds

The website only renders HTML pages behind a browser session; it has **no API** (`routes/api.php` is empty). So the work is:

1. **Backend:** add a versioned JSON API (`/api/v1`, Sanctum tokens) inside the existing Laravel app, reusing its business logic through shared service classes. Contract and step-by-step guide: `docs/API.md`.
2. **App:** this Compose Multiplatform project, which talks only to that API.

Stock and pricing rules stay on the server. The app mirrors the POS cart arithmetic so totals appear instantly, and the server re-checks every sale.

## Tech stack

| Concern | Choice |
|---|---|
| Language / UI | Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 Expressive (1.12.0-alpha03) |
| Architecture | Multi-module, offline-cached (Room is the source of truth for reads), unidirectional data flow |
| DI | Koin 4.2.2, **Koin Annotations + Koin Compiler Plugin 1.2.3** (compile-time checked; no KSP for Koin) |
| Network | Ktor client 3.6.1 (OkHttp / Darwin), kotlinx.serialization 1.12.0 |
| Local DB | Room 2.8.5 (KMP) + bundled SQLite |
| Preferences | DataStore 1.3.0 (+ Keystore/Keychain for the auth token) |
| Navigation | Navigation 3 for Compose Multiplatform 1.2.0 |
| Images / logging | Coil 3, Kermit |
| Build | Gradle version catalog, convention plugins in `build-logic`, AGP 9.4.0 |

Full version list: `docs/ARCHITECTURE.md` §15.

## Repository layout

```
PBSF/
├── androidApp/                 Android application (Application, MainActivity)
├── iosApp/                     Xcode project (SwiftUI shell hosting the Compose UI)
├── composeApp/                 shared root: App(), session gate, navigation, initKoin()
├── build-logic/convention/     pos.kmp.library, pos.kmp.compose, pos.kmp.feature, pos.koin, pos.room, pos.android.application
├── core/
│   ├── common/                 AppResult, AppError, dispatchers, application scope
│   ├── model/                  Money, Shop, SellableItem, Cart, Sale, enums
│   ├── network/                Ktor client, APIs, DTOs
│   ├── database/               Room database, entities, DAOs
│   ├── datastore/              preferences, token store
│   ├── data/                   repositories, sync
│   ├── domain/                 CartCalculator, pricing rules, use cases
│   ├── designsystem/           theme, components, Nunito font
│   ├── ui/                     formatters, shared UI helpers, platform UI bridges
│   └── testing/                fakes, fixtures, test helpers
├── feature/
│   ├── auth/  dashboard/  pos/  sales/  products/
│   ├── customers/  cashregister/  ledger/  settings/
│   └── …later phases (purchases, returns, expenses, quotations, transfers, reports, accounting, hrm)
├── docs/
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

## Getting started

Prerequisites: macOS with Xcode 26+, Android Studio with the Kotlin Multiplatform plugin (AGP 9.4.0 support), JDK 17, an Android emulator or device, an iOS simulator. Gradle must run on JDK 17, and the Xcode build needs `local.properties` with `sdk.dir` (or `ANDROID_HOME`) because its build phase runs Gradle.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :androidApp:assembleDebug        # build Android
./gradlew :androidApp:installDebug         # install on a connected device/emulator
./gradlew allTests                         # unit tests: iosSimulatorArm64Test + testAndroidHostTest in every module
open iosApp/iosApp.xcodeproj               # run on an iOS simulator from Xcode
```

Debug builds include three dev shops: **Demo shop** (answered inside the app by a debug-only network engine, so it needs no server and any username and password work; it signs in with sample data so screens can be looked at without touching a real shop), **Staging** (`https://pbsf.org.pk/smf-staging/api/v1`) and **Local API** (a server on your machine at port 8010: `10.0.2.2` from the Android emulator, `127.0.0.1` from the iOS simulator; plain HTTP is allowed only to these local addresses, only in debug builds). **Never test write actions (sales, payments, ledger edits) against a production shop.**

## Roadmap

Each phase ships the API endpoints and the app screens together. "Done" means the screen matches the web's behavior for the same data, with tests.

| Phase | Backend (Laravel) | App |
|---|---|---|
| **0 Foundation** | Staging copy; Sanctum; error envelope; `auth/login`, `auth/logout`, `me`, `bootstrap`; guarded migrations | Gradle skeleton and convention plugins; core modules; design system; shop picker; login; session gate; bootstrap cache; empty home with navigation |
| **1 Selling** | `dashboard`, `sellable-items`, `products`, `customers`, `sales` (+ receipt, payments), `gift-cards`, `cash-registers`, `ledgers`; `SaleService` extracted | Dashboard; POS (grid, search, camera + hardware scanning, cart, customer/warehouse/biller, discount, tax, shipping, coupon, previous balance, Cash/Cheque/Gift card/Deposit, drafts, recent sales, receipt + PDF share); sales list/detail/add payment; products list/detail; customers list/detail/quick add; cash register open/summary/close; ledger list/editor/import |
| **2 Stock and money in and out** | purchases, returns, expenses, quotations, transfers, adjustments, stock counts, core reports, deposits | Matching screens; offline POS outbox; Bluetooth thermal printing |
| **3 Back office** | accounting, HRM, users and roles, settings, remaining reports | Matching screens; Urdu translation |

## Decisions

Confirmed by the owner (9 November 2026):

- **Package / app id:** `pk.paksoft.pos` (Android `applicationId` and iOS bundle id). Permanent after the first store release.
- **One app for the three shops** (`smf`, `bsf`, `mummyandme`) with a shop picker. The Pakistan Blind Sports Federation WordPress site is not part of the app.
- **App first, API later.** The website is finished and stays separate from this repo. Phase 0 builds the app; the `/api/v1` backend work is planned separately (kept outside this public repo, in `PAKSOFTSolutionsPvtLtd/backend/plans/`) and done when the app needs live data.
- **Code lives at** https://github.com/nzdeveloper009/PAKSoft-POS (public). Never commit server details, backups, credentials or customer data.
- **Version control:** local git repository in this folder, one commit per finished, building step.

### Still to confirm

These are assumptions written into the docs so work can start. Each is my call, not a confirmed decision; changing one changes what gets built.

1. **Online selling in Phase 1**, with catalog and customers cached for speed; the offline sales queue comes in Phase 2.
2. **API inside the existing Laravel apps** (Sanctum 2.x), developed on a staging copy, rolled out `smf` → `bsf` → Mummy & Me.
3. **Payments in the app:** Cash, Cheque, Gift card, Deposit. Card (Stripe) and PayPal stay web-only for now.
4. **Customer-role logins** (role 5) are not supported in the app.
5. **Ledger edits** use conflict detection (`409`) instead of the web's last-write-wins, because a phone and the website may edit the same file.
6. **Minimum versions:** Android 8.0 (API 26), iOS 16.
7. **English first**; Urdu in Phase 3. The web's other 13 languages are not planned.
8. **The unreachable second ledger** (`LedgerFileController`) is ignored.

## Security notes

A review of the live server on 9 November 2026 found private files (database backups, logs, a full-site backup) that could be downloaded from the web. The owner has the details; fix them before any API goes live, and never commit backups, `.env` files or logs to this repository.

Rules for this repo: no credentials, tokens or `.env` values in code, docs, tests or commits. The app holds only a per-user token, stored in the Keystore/Keychain.
