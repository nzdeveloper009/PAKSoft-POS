# Phase 1b: Dashboard (design addendum)

**Status:** decisions below made with the owner on 11 November 2026. This addendum builds on the Phase 1 look-and-motion spec (`2026-11-10-phase1-look-and-motion-design.md`), which still governs colour, type, shape and motion. The visual reference is the owner-approved preview's Dashboard (`#s-home`: greeting, period chips, revenue hero with a profit pill, return tiles, cash-flow bars, recent sales, New sale FAB). Where this file and the preview differ, this file wins.

The owner approved the new Gradle module **`feature:dashboard`** (11 November 2026) as the next step.

## Intent

Replace the Dashboard placeholder tab with the web dashboard, same figures for the same data, in the Phase 1 look: numbers that count up, bars that grow, and a New sale button that will open the POS. The screen works offline from the last cached answer.

## What the screen shows (matches the web, `HomeController::index` + `index.blade.php`)

No permission gates (the web has none). Staff with `own` access see only their own rows (server-side). The **New sale** FAB is the only gated element: shown only with `sales-add`.

| # | Section | Data (`GET /dashboard`, `API.md` D1) | Period chips change it? |
|---|---|---|---|
| — | Header: shop avatar, shop title, "user · role", refresh icon button | session, bootstrap title | — |
| — | Greeting "Good morning/afternoon/evening, {user}" | device local hour (5–11 morning, 12–16 afternoon, otherwise evening) | — |
| a | Period chips: Today · 7 days · This month (default) · This year | `period` query | — |
| a | Revenue hero ("Revenue · this month") with a Profit pill; two tiles: Sale returns, Purchase returns | `revenue`, `profit`, `sale_return`, `purchase_return` | **yes (only these four)** |
| b | Cash flow: grouped bars, 7 months, Received vs Paid out | `cash_flow[7]` | no |
| c | This month: donut of Purchase / Revenue / Expense | `month_breakdown` | no |
| d | This year: grouped bars, 12 months, Sold vs Purchased | `yearly[12]` (new) | no |
| e | Recent transactions: segmented tabs Sales · Purchases · Quotations · Payments, 5 rows each | `recent{…}` (new) | no |
| f | Best sellers (Top 5): segmented This month (qty) · This year (qty) · By amount (this year) | `best_sellers{…}` (new) | no |

Row content follows the web's columns: Sales and Quotations show date, reference, customer, status, grand total; Purchases show date, reference, supplier (missing → "N/A", as the web), status, grand total; Payments show date, payment reference, amount, paid by. Best sellers show rank, product name, `[code]`, and quantity or amount. Rows are not tappable (no detail screens yet).

Money on the Dashboard is shown in whole rupees, `Rs 1,234` (half-up), like the preview; chart peak labels use the compact form `Rs 482k`. Dates use the shop's `date_format` from bootstrap.

## States

Every state reads from Room; the network only refreshes it.

| State | When | Shows |
|---|---|---|
| Loading | no cached dashboard yet and no failure | skeleton of header, hero, tiles, a chart card and rows (shimmer; announced once as "Loading dashboard") |
| Error | no cached dashboard and the refresh failed | `EmptyState` with the error message and Retry |
| Empty | cached, but the shop has no transactions at all (every list empty, every chart and figure zero) | header, chips, hero at Rs 0, then an empty-state card instead of charts, recent and best sellers |
| Content | otherwise | everything; a failed refresh adds an `ErrorBanner` with Retry above the hero and keeps the cached content |

A period with no cached figures yet shows skeleton blocks in the hero and tiles only; the other sections stay. `Unauthenticated` never shows a banner (the session gate handles it). The banner stays until a refresh succeeds.

Refresh happens when the Dashboard is shown, on pull-to-refresh, on the header refresh button, on Retry, and on every period change. A newer period choice cancels the older refresh.

## Motion moments

All motion from `PosTheme.motion`; under reduced motion every element shows its final state at once.

| Moment | Animation |
|---|---|
| Entering | sections `enterStagger` (header, greeting, chips, hero, tiles, then each card) |
| Figures | hero, pill and tiles count up with `animateCountAsState` (from 0 the first time, then from the shown value when the period changes) |
| Cash flow and yearly bars | each bar grows from the baseline on `spatial`, staggered 35 ms per bar after a 120 ms start (`PosMotion.chartStartDelayMillis`, `chartStaggerMillis`); the latest month's peak label fades in |
| Donut | the arcs sweep in on `slowSpatial` |
| Period chip | `PosFilterChip`'s check grows in |
| Pull to refresh | Material 3 Expressive pull-to-refresh with the morphing loading indicator |
| New sale FAB | extended FAB on `tertiaryContainer` (16 dp corners); scales in after the sections (`fastSpatial`); collapses to the icon while scrolling down, expands when scrolling up or at the top |
| New sale → POS | container transform: the FAB's bounds (`posSharedBounds(newSaleKey())`) grow into the POS tab's placeholder, and back. The FAB lives inside the Dashboard entry (not `MainLayout`) so it uses `NavDisplay`'s animated scope. Only when opened from the FAB; a tab tap still fades through. If the transform cannot run (scope missing, reduced motion) it is a fade through. |

The `RollingText` odometer is not used here (figures count; the odometer is for the POS total). Its retarget pause stays a POS-plan item.

## Decisions

1. **Match the web:** all sections (a)–(f). Period changes only the four figures, as on the web.
2. **Contract first:** `API.md` D1 keeps its fields and adds `yearly`, `recent`, `best_sellers`; `cash_flow` is corrected to 7 months (the web's loop runs from six months back through the current month). One endpoint, always the full payload; no "figures only" variant. `warehouse_id` is dropped from D1 (the web dashboard has no warehouse filter). An unknown `period` is `422 validation_failed`.
3. **Profit is FIFO for every period** (deliberate fix, recorded in `DOMAIN.md` §9). The web's page load uses `revenue + purchase_return − FIFO product cost` (`HomeController.php` lines 122 and 167), but its period buttons call `dashboardFilter`, which uses `revenue + purchase_return − Σ purchases.grand_total` (lines 258 and 271), so "This month" shows two different profits. The API uses the FIFO formula for every period.
4. **Revenue rule corrected in `DOMAIN.md` §9:** Revenue = Σ `sales.grand_total` − Σ `returns.grand_total` in the period (lines 162–165), not Σ `product_sales.total`.
5. **Offline first:** Room caches the figures per period (four rows at most) and the period-independent overview (latest). Reads come only from Room.
6. **Demo shop:** `DemoServer` answers `GET dashboard` with invented solar-shop data per period. `DemoServer.respond` now takes a request with the query parameters (Phase 1a carry-over); request bodies come with the first write endpoint (POS).
7. **One "keep last values during session exit" pattern: hold in the ViewModel.** Logging out clears Room while the home is still animating out. Screen ViewModels hold their last content (`Flow<T?>.holdLastNonNull()` in `core:common`; `MoreViewModel.withSession` is the same rule for session state). A ViewModel lives for one session, so nothing stale crosses into the next login. `MainScaffold`'s composable `rememberLastNonNull` moves into `MainViewModel` (settings held per session). Documented in `ARCHITECTURE.md` §9.
8. **Naming (Phase 1a carry-over):** `shopTitle` is the shop name shown on screen (bootstrap title, else the app's list name); `shopDisplayName` is `Shop.displayName`, the input to `ShopBrand.forShop`. `MoreUiState.shopName` is renamed `shopTitle`.
9. **Money formatting** lives in `core:ui` (`formatMoneyWhole`, `formatMoneyCompact`): `Rs` for PKR (and when no currency is cached), otherwise the currency code, at the shop's prefix/suffix position. Detail screens later keep the web's two decimals.
10. **Hero colour:** `primary` with `onPrimary`, as in the approved preview, with a `Cookie12Sided` shape at 10% `onPrimary` in its top-end corner (built inside `core:designsystem`, which owns the Expressive opt-in).
11. **Login Back (Phase 1a parked item):** system Back during a running login is consumed and does nothing, instead of leaving the app.
12. **Accessibility carry-overs taken here:** section titles are headings; skeletons hide from screen readers and the loading container announces itself; charts carry a text description of their values; the shop picker title and the login shop name become headings.

## Non-goals

- Tapping a row or a best seller (no detail screens yet); custom date ranges (`start_date`/`end_date` stay in the contract, unused by the app).
- Two-pane or tablet-specific layout (content keeps `PosLayout.contentMaxWidth`).
- The POS screen itself; the FAB targets the POS tab placeholder until POS exists.
- Other Phase 1a carry-overs that belong to later screens: a tab tap from a pushed detail slides instead of fading (Sales plan), `RollingText` retarget pause (POS plan).
- Urdu, widgets, charts with axes, gridlines or tooltips.
