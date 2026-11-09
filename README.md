# PBSF POS: Android & iOS app

A Compose Multiplatform app (Android + iOS) that brings the **SalePro point of sale and inventory system** of three shops (**Super Madina Solar Fan `smf`, BSF `bsf`, and Mummy & Me `mummyandme`**) to phones and tablets: same shops, same data, same rules as the website.

> **Scope:** the app is only for these three POS shops. The WordPress website at the root of `pbsf.org.pk` (Pakistan Blind Sports Federation) is **not** part of the app; it only shares the same hosting account. "PBSF" is just this project's folder name.

**Status (9 November 2026):** documentation only. No app or API code exists yet. Next step: Phase 0 (see Roadmap).

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
| Language / UI | Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 |
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
│   ├── common/                 AppResult, AppError, dispatchers, clock
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

Prerequisites: macOS with Xcode 26+, Android Studio (current stable) with the Kotlin Multiplatform plugin, JDK 17+, an Android emulator or device, an iOS simulator.

These commands apply once Phase 0 has created the project:

```bash
./gradlew :androidApp:assembleDebug        # build Android
./gradlew :androidApp:installDebug         # install on a connected device/emulator
./gradlew allTests                         # all common + platform unit tests
./gradlew check                            # tests + lint
open iosApp/iosApp.xcodeproj               # run on an iOS simulator from Xcode
```

Debug builds include a **Staging** shop pointing at `https://pbsf.org.pk/smf-staging/api/v1`. **Never test write actions (sales, payments, ledger edits) against a production shop.**

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
