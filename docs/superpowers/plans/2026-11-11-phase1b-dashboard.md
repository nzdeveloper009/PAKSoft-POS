# Phase 1b: Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the Dashboard placeholder with the web dashboard (figures by period, cash flow, month donut, yearly bars, recent transactions, best sellers), cached in Room, served by the Demo shop, in the Phase 1 look, with a New sale FAB that container-transforms into the POS tab.

**Architecture:** Contract first: `API.md` D1 grows `yearly`, `recent`, `best_sellers`; `core:network` decodes it and `DemoServer` answers it (now seeing the query string). `core:database` caches figures per period plus one period-independent overview; `DashboardRepository` (`core:data`) exposes Room flows and a `refresh(period)` guarded by `SessionEpoch`. The new `feature:dashboard` module holds the ViewModel and screen; charts, the FAB, pull-to-refresh and other generic pieces are added to `core:designsystem`, formatters to `core:ui`.

**Tech Stack:** Kotlin 2.4.21, Compose Multiplatform 1.12.2, Material 3 `1.12.0-alpha03` (Expressive), Navigation 3 1.2.0, Ktor 3.6.1, Room 2.8.5, Koin 4.2.2 Annotations + Compiler Plugin, Turbine.

**Spec:** `docs/superpowers/specs/2026-11-11-phase1b-dashboard-design.md` (decisions, states, motion moments), on top of `docs/superpowers/specs/2026-11-10-phase1-look-and-motion-design.md` (colour, type, shape, motion tokens). Business rules: `docs/DOMAIN.md` §9 as rewritten in Task 2.

## Global Constraints

- Every Gradle command is prefixed `JAVA_HOME=$(/usr/libexec/java_home -v 17)`. AGP stays at exactly 9.4.0.
- No new dependencies and no version changes. One new module, `feature:dashboard` (owner-approved 11 November 2026). No new top-level destinations (the Dashboard tab exists).
- Contract first: `docs/API.md` changes land before the Kotlin that implements them (Task 2), and DTO tests decode the `API.md` example verbatim.
- `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` appears only in `core:designsystem`; features use its wrappers.
- No literal colours, durations, easings, spring values or spacing outside `core:designsystem`. Motion goes through `PosTheme.motion`; charts are designsystem components fed scheme roles (`colorScheme.*`, `PosTheme.colors.chartIn/chartOut`).
- Money is `Money` (Long paisa) in models, entities, DTOs and UI state; JSON money is a 2-decimal string via `MoneySerializer`; Room money columns are `Long`.
- All user-visible text in the owning module's `composeResources/values/strings.xml`; apostrophes written plainly; ViewModels emit `UiText`.
- Follow `CLAUDE.md` (Koin triad, `@Singleton` not `@Single`, no `!!`, `XxxRoute` + stateless `XxxScreen` + light and dark `@Preview`s, `collectAsStateWithLifecycle`, `stateIn(WhileSubscribed(5_000))`, rethrow `CancellationException`, every new `NavKey` in `navSavedStateConfig`).
- Never contact a production shop. Manual checks use the Demo shop. The legacy web (`../pbsf.org.pk/`) is read-only.
- One commit per task (fix rounds may add commits); push to `origin main` only after the task review passes. Commit messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Done = `./gradlew allTests` green, `:androidApp:assembleDebug` and `:composeApp:linkDebugFrameworkIosSimulatorArm64` succeed, and UI tasks are looked at on the Android emulator (Demo shop, light and dark) with screenshots saved to `/Users/syed/AndroidStudioProjects/PAKSOFTSolutionsPvtLtd/phase1-review/screens/` (outside the repo) via `adb exec-out screencap -p > <path>`. Screenshot names start with `1b-`.

## Review Focus

1. **Fast period taps:** tapping Today, then This year before the first answer lands must end showing This year's figures under the "this year" label, never an older answer under a newer label. Test: `DashboardViewModelTest` "rapid period changes keep only the last refresh" (Task 5); per-period cache rows (Task 3).
2. **Logout or switch shop while the Dashboard shows:** the leaving screen keeps its last content (no blank flash, no skeleton), and a refresh answer that lands after the clear must not refill the cache. Tests: `HoldLastNonNullTest`, `DashboardViewModelTest` "cleared cache keeps the last content" (Task 5); `DefaultDashboardRepositoryTest` "refresh answering after switch shop does not refill the cache" (Task 4); `MainViewModelTest` "settings stay during the logout exit" (Task 5).
3. **Offline first run:** no cache plus a failed refresh must show the Error state with Retry, not an endless skeleton; with a cache it shows content plus the banner. Tests: `DashboardViewModelTest` "failed first load without cache is the error state" and "failed refresh with cache keeps content and shows the banner" (Task 5).
4. **Losses and big numbers:** a negative profit, a Rs 10 crore year and an all-zero chart must format and draw without crashing or dividing by zero. Tests: `MoneyFormatTest` negatives and crores (Task 6), `BarChartTest` all-zero and negative (Task 6), `DonutChartTest` all-zero (Task 7).
5. **Partial or odd server data:** null customer or supplier, unknown status or payment method strings, fewer than 5 rows or fewer than 7 months must decode and render. Tests: `DashboardDtoTest` "unknown enum strings and null parties decode" (Task 2), `DashboardMappersTest` unknown → `Unknown` (Task 4), `RecentRowsTest` null supplier kept as no party, shown as "N/A" (Task 7).

---

### Task 1: Login consumes Back while a login runs

**Files:**
- Modify: `feature/auth/src/commonMain/kotlin/pk/paksoft/pos/feature/auth/login/LoginUiState.kt`, `login/LoginRoute.kt`, `login/LoginScreen.kt` (shop name heading), `shop/ShopPickerScreen.kt` (title heading)
- Test: `feature/auth/src/commonTest/kotlin/pk/paksoft/pos/feature/auth/LoginViewModelTest.kt`
- Docs: `docs/ARCHITECTURE.md` §7 "Back on the gate" bullet

**Interfaces:**
- Produces: `enum class LoginBack { ChangeShop, Ignore }` and `val LoginUiState.back: LoginBack` (`ChangeShop` when `canChangeShop`, else `Ignore`), in `LoginUiState.kt`.

- [ ] **Step 1: Write the failing tests** in `LoginViewModelTest`:
  - `` `back changes shop when no login runs`() ``: fresh VM → `state.value.back == LoginBack.ChangeShop`.
  - `` `back is ignored while a login runs`() ``: `fake.loginGate = CompletableDeferred()`, fill, `Submit` → `back == LoginBack.Ignore`; then the route's rule (`if (state.back == ChangeShop) onAction(ChangeShop)`) applied → `fake.switchShopCalls == 0`, `isSubmitting` still true, password kept.
  - `` `back is ignored after a successful login while home takes over`() ``: fill, `Submit` (fake succeeds) → `back == LoginBack.Ignore`.
  - Replace `the back arrow and system back change shop only while no login runs` and `back stays off after a successful login while home takes over` with the above (same scenarios, new property).
- [ ] **Step 2: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :feature:auth:testAndroidHostTest` → compilation failure (`LoginBack` missing).
- [ ] **Step 3: Implement.** `LoginRoute`: `NavigationBackHandler(state = rememberNavigationEventState(NavigationEventInfo.None), isBackEnabled = true, onBackCompleted = { if (state.back == LoginBack.ChangeShop) vm.onAction(LoginAction.ChangeShop) })`, KDoc: Back always belongs to the login; it changes shop, or is swallowed while a login runs so a cashier can't leave the app mid-login. Heading semantics (`Modifier.semantics { heading() }`) on the shop picker title and the login header's shop name.
- [ ] **Step 4: Run tests, builds, look at it.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :feature:auth:allTests :androidApp:assembleDebug` → BUILD SUCCESSFUL. Emulator, Demo shop login: type any text, tap Log in and immediately press Back (`adb shell input keyevent KEYCODE_BACK` right after the tap) → the app stays on the login and then reaches home; on an idle login Back returns to the shop picker. Save `1b-t1-login-back.png` (home reached).
- [ ] **Step 5: Commit**

```bash
git add -A feature/auth docs/ARCHITECTURE.md
git commit -m "fix(auth): Back during a running login is consumed instead of leaving the app"
```

---

### Task 2: D1 contract, DTOs, Ktor API and the Demo dashboard

**Files:**
- Docs: `docs/API.md` D1 (rewrite), `docs/DOMAIN.md` §3 (purchase, quotation, payment method enums) and §9 (rewrite), `docs/ARCHITECTURE.md` §5 (DemoServer sees the query), `CLAUDE.md` recipe "Add an API call" step 4
- Create: `core/network/src/commonMain/kotlin/pk/paksoft/pos/core/network/dto/DashboardDtos.kt`, `api/DashboardApi.kt`, `api/KtorDashboardApi.kt`, `demo/DemoDashboard.kt` (demo JSON per period)
- Modify: `core/network/.../demo/DemoServer.kt`, `demo/DemoAwareEngine.kt`
- Create: `core/model/src/commonMain/kotlin/pk/paksoft/pos/core/model/DashboardPeriod.kt` (the request enum, needed by the API)
- Test: `core/network/src/commonTest/.../ApiFixtures.kt` (add `DASHBOARD_200`), `DashboardDtoTest.kt`, `api/KtorDashboardApiTest.kt`, `demo/DemoServerTest.kt`, `demo/DemoAwareEngineTest.kt`

**Interfaces:**
- Produces (`core:model`): `enum class DashboardPeriod(val apiValue: String) { Today("today"), Last7Days("last_7_days"), ThisMonth("this_month"), ThisYear("this_year") }`.
- Produces (`core:network`, `dto` package, all `@Serializable`, every property `@SerialName`, money via `MoneySerializer`):
  `DashboardDto(period: DashboardRangeDto, revenue: Money, saleReturn: Money, purchaseReturn: Money, profit: Money, cashFlow: List<CashFlowMonthDto>, monthBreakdown: MonthBreakdownDto, yearly: List<YearlyMonthDto>, recent: RecentDto, bestSellers: BestSellersDto)`;
  `DashboardRangeDto(startDate: String, endDate: String)`; `CashFlowMonthDto(month: String, received: Money, sent: Money)`; `MonthBreakdownDto(month: String, purchase: Money, revenue: Money, expense: Money)`; `YearlyMonthDto(month: String, sold: Money, purchased: Money)`;
  `RecentDto(sales: List<RecentSaleDto>, purchases: List<RecentPurchaseDto>, quotations: List<RecentQuotationDto>, payments: List<RecentPaymentDto>)`;
  `RecentSaleDto(id: Long, date: String, referenceNo: String, customer: PartyRefDto?, status: String, grandTotal: Money)`; `RecentPurchaseDto(…, supplier: PartyRefDto?, …)`; `RecentQuotationDto(…, customer: PartyRefDto?, …)`; `RecentPaymentDto(id: Long, date: String, paymentReference: String, amount: Money, payingMethod: String)`; `PartyRefDto(id: Long, name: String)`;
  `BestSellersDto(monthQty: List<BestSellerQtyDto>, yearQty: List<BestSellerQtyDto>, yearAmount: List<BestSellerAmountDto>)`; `BestSellerQtyDto(product: ProductRefDto, qty: Double)`; `BestSellerAmountDto(product: ProductRefDto, amount: Money)`; `ProductRefDto(id: Long, name: String, code: String)`.
  Status and method stay `String` in DTOs; Task 4 maps them to enums with `Unknown`.
- Produces: `interface DashboardApi { suspend fun dashboard(period: DashboardPeriod): AppResult<DashboardDto> }`; `@Singleton class KtorDashboardApi(client, caller, endpoint) : DashboardApi` (GET `dashboard` with `parameter("period", period.apiValue)`).
- Produces: `internal data class DemoRequest(val method: HttpMethod, val encodedPath: String, val query: Parameters = Parameters.Empty)`; `DemoServer.respond(request: DemoRequest): DemoResponse` (replaces the two-argument form; body support comes with the first write endpoint). `DemoAwareEngine` passes `data.url.parameters`.

- [ ] **Step 1: Write the contract.** In `API.md` D1:
  - Query: `period=today|last_7_days|this_month|this_year` (default `this_month`) **or** `start_date` + `end_date`. Drop `warehouse_id` (the web dashboard has none). Unknown `period` → `422 validation_failed`, `fields.period`.
  - Period windows (from `index.blade.php` lines 104–107): today = today..today; last_7_days = today − 7 days..today (8 calendar days, as the web); this_month = 1st..last day of the month; this_year = 1 Jan..31 Dec. `period` echoes the window used.
  - Only `revenue`, `sale_return`, `purchase_return`, `profit` depend on the period; everything else is the same for every period.
  - Example (keep the four existing figures; `cash_flow` has **7** entries `2026-05`..`2026-11`; `yearly` has 12 entries `2026-01`..`2026-12`; `recent` lists one row per kind, with one purchase whose `supplier` is `null`; `best_sellers` one row per list). Shapes:
    ```json
    "yearly": [ { "month": "2026-01", "sold": "0.00", "purchased": "0.00" } ],
    "recent": {
      "sales": [ { "id": 4, "date": "2026-11-08", "reference_no": "posr-20261108-020720", "customer": { "id": 1, "name": "Walk in Customer" }, "status": "completed", "grand_total": "7998.00" } ],
      "purchases": [ { "id": 2, "date": "2026-11-05", "reference_no": "pr-20261105-101500", "supplier": null, "status": "received", "grand_total": "120000.00" } ],
      "quotations": [ { "id": 1, "date": "2026-11-02", "reference_no": "qr-20261102-110000", "customer": { "id": 1, "name": "Walk in Customer" }, "status": "pending", "grand_total": "3999.00" } ],
      "payments": [ { "id": 9, "date": "2026-11-08", "payment_reference": "spr-20261108-020720", "amount": "7998.00", "paying_method": "cash" } ]
    },
    "best_sellers": {
      "month_qty": [ { "product": { "id": 12, "name": "Pedestal Fan 14", "code": "PF14" }, "qty": 2 } ],
      "year_qty": [ { "product": { "id": 12, "name": "Pedestal Fan 14", "code": "PF14" }, "qty": 2 } ],
      "year_amount": [ { "product": { "id": 12, "name": "Pedestal Fan 14", "code": "PF14" }, "amount": "15996.00" } ]
    }
    ```
  - Rules text: recent lists are the latest 5 by id of each kind, all statuses; `status` values: sales `completed|pending|draft`, purchases `received|partial|pending|ordered`, quotations `pending|sent`; `paying_method` `cash|gift_card|credit_card|cheque|paypal|deposit`; `customer`/`supplier` is `null` when the row is missing; best sellers whose product row is missing are skipped; best sellers ignore the staff `own` scope (as the web).
- [ ] **Step 2: Rewrite `DOMAIN.md` §9** with sources (`HomeController.php` line numbers), and add the three enums to §3 (purchases.status `1` Received · `2` Partial · `3` Pending · `4` Ordered; quotations.quotation_status `1` Pending · `2` Sent; payments.paying_method strings → API values; source `resources/views/index.blade.php`). §9 content:
  - Who: every logged-in user, no permission check; staff `own` scope (`role_id > 2` and `staff_access = own`) filters figures, charts and recent lists by `user_id` (profit's sold products by `sales.user_id`); best sellers are not scoped (lines 188–192).
  - Revenue = Σ `sales.grand_total` − Σ `returns.grand_total` (lines 162–165). Sale return = Σ `returns.grand_total`; purchase return = Σ `return_purchases.grand_total`.
  - Profit = revenue + purchase return − FIFO product cost (lines 130–167). FIFO, per (`product_id`, `product_batch_id`) of the period's `product_sales` with `sold_qty = Σ qty`: walk that product's (and batch's, when set) `product_purchases` in id order, adding `qty` and `total`; at the first row where the running qty ≥ `sold_qty`, subtract `(running qty − sold_qty) × (row total / row qty)` and stop; if the purchases never reach `sold_qty`, cost is the sum of all of them. Each period starts from the first purchase.
  - **Deliberate fix:** the web's period buttons use `dashboardFilter` (lines 258, 271): profit = revenue + purchase return − Σ `purchases.grand_total`, so the same month shows two different profits. The API uses the FIFO profit for every period.
  - Cash flow (lines 194–225): 7 months, from the 1st of the month six months back through the current month. Received = Σ `payments.amount` with `sale_id` + Σ `return_purchases.grand_total`; sent = Σ `payments.amount` with `purchase_id` + Σ `returns.grand_total` + Σ `expenses.amount` + Σ `payrolls.amount`.
  - This month donut: purchase (Σ `purchases.grand_total`), revenue (as above) and expense (Σ `expenses.amount`) of the current month; not changed by the period buttons.
  - Yearly (lines 226–244): each month of the current year, sold = Σ `sales.grand_total`, purchased = Σ `purchases.grand_total`.
  - Recent (lines 169–172): latest 5 by id of sales, purchases, quotations, payments; columns as `API.md` D1. Best sellers (lines 188–192): top 5 `product_sales` by Σ qty this month, by Σ qty this year, by Σ total this year.
- [ ] **Step 3: Write the failing tests.**
  - `ApiFixtures.DASHBOARD_200` = the D1 example verbatim.
  - `DashboardDtoTest`: `` `dashboard example decodes`() `` (revenue `Money(1_599_600)`, profit `Money(609_400)`, `cashFlow.size == 7`, `yearly.size == 12`, `recent.purchases[0].supplier == null`, `bestSellers.monthQty[0].product.code == "PF14"`, `bestSellers.yearAmount[0].amount == Money(1_599_600)`); `` `unknown enum strings and null parties decode`() `` (a sale with `"status": "archived"` and `"customer": null`, a payment with `"paying_method": "bitcoin"` decode; strings kept as-is); `` `empty lists decode`() ``.
  - `KtorDashboardApiTest` (MockEngine): `` `sends the period as a query parameter`() `` (request URL ends with `/dashboard?period=last_7_days`); `` `decodes the example`() ``; `` `validation error maps to Validation`() `` (422 envelope → `AppError.Validation` with field `period`).
  - `DemoServerTest`: `` `dashboard answers each period with its own figures`() `` — for `period=today|last_7_days|this_month|this_year` the decoded revenue/profit/sale_return/purchase_return are `18750/2940/0/0`, `126400/16880/1200/4500`, `482350/61240/3450/12800`, `5184900/702300/41600/96250` rupees, and the overview (cash flow, yearly, recent, best sellers) is identical across periods; `` `dashboard without a period is this month`() ``; `` `unknown period is a validation error`() `` (`period=decade` → 422, `ErrorEnvelopeDto.code == "validation_failed"`); existing tests move to `respond(DemoRequest(...))`.
  - `DemoAwareEngineTest`: `` `demo requests carry their query string`() `` (`KtorDashboardApi` against `Shops.demo` with `ThisYear` → revenue `Money(518_490_000)`).
- [ ] **Step 4: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:network:testAndroidHostTest` → compilation failures.
- [ ] **Step 5: Implement.** DTOs and `KtorDashboardApi` per Interfaces (`caller.call(DashboardDto.serializer()) { client.get(endpoint.url("dashboard")) { parameter("period", period.apiValue) } }`). `DemoServer`: route `GET dashboard`; read `query["period"] ?: "this_month"`; unknown → 422 `{"error":{"code":"validation_failed","message":"Please check the highlighted fields.","fields":{"period":["Unknown period."]}}}`. `DemoDashboard`: invented solar-shop data, fixed in 2026 (today = 2026-11-10): the figures above; cash flow `2026-05`..`2026-11` with received rising to `482350.00` in November and sent below it every month; yearly with zero for December; 5 recent rows per kind (customers such as "Walk in Customer", "Bilal Traders"; suppliers such as "Sun Power Wholesale", one `null`; references in the web's `posr-`/`pr-`/`qr-`/`spr-` style; mixed statuses and methods); 5 best sellers per list from a solar catalog (pedestal fans, 550 W panels, 100 Ah lithium batteries, 20 W LED tubes, 3 kW inverters). No real names from any shop.
- [ ] **Step 6: Run tests and builds.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:network:allTests :core:model:allTests :androidApp:assembleDebug` → BUILD SUCCESSFUL.
- [ ] **Step 7: Commit**

```bash
git add -A docs CLAUDE.md core/model core/network
git commit -m "feat(network): dashboard contract with yearly, recent and best sellers; Demo shop answers it per period"
```

---

### Task 3: Dashboard models and Room cache

**Files:**
- Create: `core/model/src/commonMain/kotlin/pk/paksoft/pos/core/model/Dashboard.kt`, `core/model/.../TransactionEnums.kt`
- Create: `core/database/src/commonMain/kotlin/pk/paksoft/pos/core/database/entity/DashboardEntities.kt`, `dao/DashboardDao.kt`
- Modify: `core/database/.../PosDatabase.kt` (entities, `dashboardDao()`, `version = 2`), `di/DatabaseModule.kt` (`@Singleton fun dashboardDao(db)`)
- Create (generated): `core/database/schemas/pk.paksoft.pos.core.database.PosDatabase/2.json`
- Test: `core/database/src/iosTest/kotlin/pk/paksoft/pos/core/database/DashboardDaoTest.kt`
- Docs: `docs/ARCHITECTURE.md` §6.1 (dashboard tables, version 2, still a destructive-migration cache)

**Interfaces:**
- Produces (`core:model`):
  - `enum class SaleStatus { Completed, Pending, Draft, Unknown }`, `enum class PurchaseStatus { Received, Partial, Pending, Ordered, Unknown }`, `enum class QuotationStatus { Pending, Sent, Unknown }`, `enum class PaymentMethod { Cash, GiftCard, CreditCard, Cheque, Paypal, Deposit, Unknown }`.
  - `data class DashboardFigures(val period: DashboardPeriod, val startDate: String, val endDate: String, val revenue: Money, val saleReturn: Money, val purchaseReturn: Money, val profit: Money)`.
  - `data class DashboardOverview(val cashFlow: List<CashFlowMonth>, val monthBreakdown: MonthBreakdown, val yearly: List<YearlyMonth>, val recent: RecentTransactions, val bestSellers: BestSellers)`; `CashFlowMonth(month: String, received: Money, sent: Money)`; `MonthBreakdown(month: String, purchase: Money, revenue: Money, expense: Money)`; `YearlyMonth(month: String, sold: Money, purchased: Money)`; `RecentTransactions(sales: List<RecentSale>, purchases: List<RecentPurchase>, quotations: List<RecentQuotation>, payments: List<RecentPayment>)`; `RecentSale(id: Long, date: String, referenceNo: String, customerName: String?, status: SaleStatus, grandTotal: Money)`; `RecentPurchase(id, date, referenceNo, supplierName: String?, status: PurchaseStatus, grandTotal)`; `RecentQuotation(id, date, referenceNo, customerName: String?, status: QuotationStatus, grandTotal)`; `RecentPayment(id: Long, date: String, reference: String, amount: Money, method: PaymentMethod)`; `BestSellers(monthQty: List<BestSellerQty>, yearQty: List<BestSellerQty>, yearAmount: List<BestSellerAmount>)`; `BestSellerQty(productId: Long, name: String, code: String, qty: Double)`; `BestSellerAmount(productId: Long, name: String, code: String, amount: Money)`. Months are `"YYYY-MM"`, dates `"YYYY-MM-DD"`.
- Produces (`core:database`, money columns `Long` paisa):
  - `dashboard_figures`: `DashboardFiguresEntity(@PrimaryKey period: String /* apiValue */, startDate, endDate, revenue: Long, saleReturn: Long, purchaseReturn: Long, profit: Long)`.
  - `dashboard_overview` (one row, `id = 1`): `DashboardOverviewEntity(id: Int = 1, breakdownMonth: String, breakdownPurchase: Long, breakdownRevenue: Long, breakdownExpense: Long)`.
  - `dashboard_months`, PK (`series`, `month`): `DashboardMonthEntity(series: String /* "cash_flow" | "yearly" */, month: String, first: Long /* received | sold */, second: Long /* sent | purchased */)`.
  - `dashboard_recent`, PK (`kind`, `position`): `DashboardRecentEntity(kind: String /* "sale" | "purchase" | "quotation" | "payment" */, position: Int, id: Long, date: String, reference: String, partyName: String?, status: String?, amount: Long, method: String?)` (API strings stored as received).
  - `dashboard_best_sellers`, PK (`list`, `position`): `DashboardBestSellerEntity(list: String /* "month_qty" | "year_qty" | "year_amount" */, position: Int, productId: Long, name: String, code: String, qty: Double?, amount: Long?)`.
  - `@Dao abstract class DashboardDao`: `observeFigures(period: String): Flow<DashboardFiguresEntity?>`; `observeOverview(): Flow<DashboardOverviewEntity?>`; `observeMonths(): Flow<List<DashboardMonthEntity>>` (`ORDER BY series, month`); `observeRecent(): Flow<List<DashboardRecentEntity>>` (`ORDER BY kind, position`); `observeBestSellers(): Flow<List<DashboardBestSellerEntity>>` (`ORDER BY list, position`); `@Transaction open suspend fun replace(figures: DashboardFiguresEntity, overview: DashboardOverviewEntity, months: List<DashboardMonthEntity>, recent: List<DashboardRecentEntity>, bestSellers: List<DashboardBestSellerEntity>)` (upsert the figures row; delete and insert the overview tables); `@Transaction open suspend fun clearAll()`.

- [ ] **Step 1: Write the failing tests** (`DashboardDaoTest`, in-memory Room like `BootstrapDaoTest`):
  - `` `replace stores figures per period and keeps other periods`() ``: replace with `today` then `this_year` figures → `observeFigures("today")` and `observeFigures("this_year")` both emit their own rows.
  - `` `replace swaps the overview tables`() ``: replace with 7 months/5 recent, then with 3 months/2 recent → months and recent hold only the second set; overview row is the second.
  - `` `rows come back in position order`() ``.
  - `` `clearAll empties every dashboard table`() ``.
- [ ] **Step 2: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:database:iosSimulatorArm64Test` → compilation failures.
- [ ] **Step 3: Implement** the models, entities, DAO and database changes per Interfaces; build once so Room exports `2.json`.
- [ ] **Step 4: Run tests and builds.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:database:allTests :core:model:allTests :androidApp:assembleDebug` → BUILD SUCCESSFUL; `git status` shows `core/database/schemas/.../2.json`.
- [ ] **Step 5: Commit**

```bash
git add -A core/model core/database docs/ARCHITECTURE.md
git commit -m "feat(database): cache dashboard figures per period and the dashboard overview"
```

---

### Task 4: DashboardRepository

**Files:**
- Create: `core/data/src/commonMain/kotlin/pk/paksoft/pos/core/data/DashboardRepository.kt`, `DefaultDashboardRepository.kt`, `mapper/DashboardMappers.kt`
- Modify: `core/data/.../RoomLocalDataCleaner.kt` (also clears `DashboardDao`)
- Create: `core/testing/src/commonMain/kotlin/pk/paksoft/pos/core/testing/FakeDashboardApi.kt`, `FakeDashboardRepository.kt`; modify `TestData.kt` (`dashboardDto()`, `dashboardFigures(period)`, `dashboardOverview()` built from the D1 example)
- Test: `core/data/src/commonTest/.../DashboardMappersTest.kt`, `core/data/src/iosTest/.../DefaultDashboardRepositoryTest.kt`

**Interfaces:**
- Consumes: `DashboardApi`, `DashboardDto` (Task 2); entities, `DashboardDao`, models (Task 3); `SessionEpoch`.
- Produces:
  - `interface DashboardRepository { fun figures(period: DashboardPeriod): Flow<DashboardFigures?>; val overview: Flow<DashboardOverview?>; suspend fun refresh(period: DashboardPeriod): AppResult<Unit> }` (`overview` is null until the first refresh writes; it combines the four overview flows).
  - `@Singleton internal class DefaultDashboardRepository(api: DashboardApi, dao: DashboardDao, epoch: SessionEpoch) : DashboardRepository` (same shape as `DefaultBootstrapRepository`: note the epoch, call the API, map, `epoch.writeIfCurrent { dao.replace(...) }`; a dropped write returns `Failure(Unexpected(null))`).
  - Mappers (`internal`): `DashboardDto.toEntities(period: DashboardPeriod): DashboardEntities`; `DashboardFiguresEntity.toModel(): DashboardFigures?` (null for an unknown period string); `toOverview(overview, months, recent, bestSellers): DashboardOverview?` (null when `overview` is null); status/method strings map to enums with `Unknown` (`"gift_card"` → `GiftCard`, `"credit_card"` → `CreditCard`).
  - `FakeDashboardRepository`: `figuresFlows: Map<DashboardPeriod, MutableStateFlow<DashboardFigures?>>`, `overviewFlow: MutableStateFlow<DashboardOverview?>`, `refreshResult: AppResult<Unit>`, `refreshGate: CompletableDeferred<Unit>?`, `refreshCalls: List<DashboardPeriod>`. `FakeDashboardApi` mirrors `FakeBootstrapApi` and records the requested periods.

- [ ] **Step 1: Write the failing tests.**
  - `DashboardMappersTest`: `` `example maps to entities and back`() `` (`TestData.dashboardDto().toEntities(ThisMonth)` → figures row `period == "this_month"`, revenue `1_599_600`; 19 month rows (7 + 12); recent positions 0.. per kind; back to models equals `TestData.dashboardFigures(ThisMonth)` / `TestData.dashboardOverview()`); `` `unknown status and method map to Unknown`() ``; `` `null parties stay null`() ``.
  - `DefaultDashboardRepositoryTest` (iosTest, in-memory Room, `FakeDashboardApi`): `` `refresh stores figures for its period and the overview`() ``; `` `figures of another period are kept`() ``; `` `failed refresh keeps the cached data`() ``; `` `refresh answering after switch shop does not refill the cache`() `` (gate the fake API, run `epoch.clear { dao.clearAll() }`, release → result `Failure`, `figures(ThisMonth).first() == null`, `overview.first() == null`); `` `local data cleaner clears the dashboard`() `` (`RoomLocalDataCleaner.clearAll()` empties both flows).
- [ ] **Step 2: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:data:testAndroidHostTest :core:data:iosSimulatorArm64Test` → compilation failures.
- [ ] **Step 3: Implement** per Interfaces.
- [ ] **Step 4: Run tests and builds.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:data:allTests :core:testing:allTests :androidApp:assembleDebug` → BUILD SUCCESSFUL (the Koin compile check proves `DashboardRepository` is bound).
- [ ] **Step 5: Commit**

```bash
git add -A core/data core/testing
git commit -m "feat(data): dashboard repository over Room with per-period refresh"
```

---

### Task 5: `feature:dashboard` module, ViewModel and navigation

**Files:**
- Modify: `settings.gradle.kts` (`include(":feature:dashboard")`); create `feature/dashboard/build.gradle.kts` (`plugins { id("pos.kmp.feature") }`)
- Create under `feature/dashboard/src/commonMain/kotlin/pk/paksoft/pos/feature/dashboard/`: `di/DashboardModule.kt` (triad), `navigation/DashboardKeys.kt` (`DashboardKey` moves here from `composeApp`), `navigation/DashboardEntries.kt`, `DashboardUiState.kt`, `DashboardViewModel.kt`, `DashboardRoute.kt`, `DashboardScreen.kt` (states only; Tasks 6–7 build the look), `src/commonMain/composeResources/values/strings.xml`
- Create: `core/common/src/commonMain/kotlin/pk/paksoft/pos/core/common/HoldLast.kt`, `WallClock.kt` (+ `androidMain`/`iosMain` actuals of `platformLocalHour()`); `core/testing/.../FixedWallClock.kt`
- Modify: `composeApp/build.gradle.kts` (dependency), `app/navigation/TopLevelKeys.kt` (remove `DashboardKey`), `app/navigation/NavSavedState.kt`, `app/main/TopLevelDestination.kt`, `app/main/MainScaffold.kt` (dashboard entries, `onNewSale`, FAB-launch flag; drop `rememberLastNonNull`), `app/main/MainViewModel.kt` (hold settings per session), `app/main/ComingSoonScreen.kt` (optional shared-bounds key), every test importing the old `DashboardKey`
- Modify: `core/designsystem/.../motion/SharedTransitions.kt` (`fun newSaleKey(): String = "new-sale"`)
- Test: `core/common/src/commonTest/.../HoldLastNonNullTest.kt`, `feature/dashboard/src/commonTest/kotlin/pk/paksoft/pos/feature/dashboard/DashboardViewModelTest.kt`, `GreetingTest.kt`, `DashboardContentTest.kt`, `composeApp/src/commonTest/.../MainViewModelTest.kt`, `composeApp/src/iosTest/.../NavSavedStateTest.kt`
- Docs: `docs/ARCHITECTURE.md` §7 (DashboardKey lives in `feature:dashboard`; FAB → POS container transform) and §9 (the keep-last pattern and the `shopTitle`/`shopDisplayName` naming rule)

**Interfaces:**
- Consumes: `DashboardRepository`, models (Tasks 3–4); `SessionRepository`, `BootstrapRepository`; `newSaleKey()`.
- Produces (`core:common`): `fun <T : Any> Flow<T?>.holdLastNonNull(): Flow<T?>` (emits nulls only until the first non-null value, then never null again); `interface WallClock { fun localHour(): Int }` with `@Singleton internal class SystemWallClock : WallClock` over `internal expect fun platformLocalHour(): Int` (Android `java.time.LocalTime.now().hour`, iOS `NSCalendar.currentCalendar.component(NSCalendarUnitHour, NSDate())`); `FixedWallClock(var hour: Int)` in `core:testing`.
- Produces (`feature:dashboard`):
  - `@Serializable data object DashboardKey : NavKey`; `fun EntryProviderScope<NavKey>.dashboardEntries(onNewSale: () -> Unit)`.
  - `enum class Greeting { Morning, Afternoon, Evening }`; `internal fun greetingFor(hour: Int): Greeting` (5–11 Morning, 12–16 Afternoon, else Evening).
  - `enum class RecentTab { Sales, Purchases, Quotations, Payments }`; `enum class BestSellerList { MonthQty, YearQty, YearAmount }`; `enum class DashboardContent { Loading, Error, Empty, Ready }`.
  - `data class DashboardUiState(val shopId: String = "", val shopTitle: String = "", val shopDisplayName: String = "", val userName: String = "", val roleName: String = "", val greeting: Greeting = Greeting.Morning, val period: DashboardPeriod = DashboardPeriod.ThisMonth, val figures: DashboardFigures? = null, val overview: DashboardOverview? = null, val currency: Currency? = null, val dateFormat: String = "d/m/Y", val recentTab: RecentTab = RecentTab.Sales, val bestSellerList: BestSellerList = BestSellerList.MonthQty, val isRefreshing: Boolean = false, val banner: UiText? = null, val canStartSale: Boolean = false) { val content: DashboardContent get() = dashboardContent(figures, overview, banner) }`.
  - `internal fun dashboardContent(figures: DashboardFigures?, overview: DashboardOverview?, banner: UiText?): DashboardContent` (overview null → `Error` if banner else `Loading`; overview with every list empty, every chart value and breakdown zero, and figures null or all zero → `Empty`; else `Ready`).
  - `sealed interface DashboardAction { Entered; Refresh; data class SelectPeriod(val period: DashboardPeriod); data class SelectRecentTab(val tab: RecentTab); data class SelectBestSellerList(val list: BestSellerList); NewSale }`; `sealed interface DashboardEffect { OpenNewSale }`.
  - `@KoinViewModel class DashboardViewModel(session: SessionRepository, bootstrap: BootstrapRepository, dashboard: DashboardRepository, clock: WallClock)`: `state` from `combine(session.state, bootstrap.settings, period.flatMapLatest { dashboard.figures(it).holdLastNonNull() }, dashboard.overview.holdLastNonNull(), local)` with `stateIn(WhileSubscribed(5_000))`; `shopTitle = settings?.title ?: shop.displayName`; `canStartSale = session.can(Permissions.SALES_ADD)`; `effects: Flow<DashboardEffect>`. One `refreshJob`: `Entered` refreshes unless one runs; `Refresh` and `SelectPeriod` cancel and restart it for the current period; success clears `banner`; failure other than `Unauthenticated` sets it.
  - `DashboardRoute(onNewSale: () -> Unit, vm: DashboardViewModel = koinViewModel())`: `LaunchedEffect(Unit) { vm.onAction(Entered) }` (runs each time the tab is shown); `ObserveEffects` → `onNewSale`.
  - `MainViewModel`: `settings` = `session.state.map { it is LoggedIn }.distinctUntilChanged().flatMapLatest { if (it) bootstrap.settings.holdLastNonNull() else emptyFlow() }`, so settings are held through the logout exit and start fresh at the next login.
  - `MainScaffold`: `dashboardEntries(onNewSale = { posFromFab = true; showTab(TopLevelDestination.POS) })`; selecting a tab from the bar sets `posFromFab = false`; `posFromFab` is `rememberSaveable`; the POS placeholder entry is `ComingSoonScreen(POS, sharedBoundsKey = if (posFromFab) newSaleKey() else null)`, which applies `posSharedBounds(key, PosSharedResize.Remeasure)` to its root surface.

- [ ] **Step 1: Write the failing tests.**
  - `HoldLastNonNullTest`: `flowOf(null, 1, null, 2, null).holdLastNonNull().toList() == listOf(null, 1, 1, 2, 2)`; leading nulls pass through.
  - `GreetingTest`: hours 4→Evening, 5→Morning, 11→Morning, 12→Afternoon, 16→Afternoon, 17→Evening, 23→Evening.
  - `DashboardContentTest`: no overview, no banner → `Loading`; no overview + banner → `Error`; overview with all empty/zero and figures zero → `Empty`; `TestData.dashboardOverview()` → `Ready`; empty overview but non-zero figures → `Ready`.
  - `DashboardViewModelTest` (fakes + Turbine, `UnconfinedTestDispatcher`, `FakeSessionRepository` logged in to `TestData.smf`):
    - `` `entering refreshes this month`() `` (`refreshCalls == [ThisMonth]`); `` `entering while a refresh runs starts no other`() ``.
    - `` `first load without cache is loading then ready`() ``.
    - `` `failed first load without cache is the error state`() `` (`content == Error`, banner = `AppError.Offline.toUiText()`), then `Refresh` with success and overview emitted → `Ready`, banner null.
    - `` `failed refresh with cache keeps content and shows the banner`() ``.
    - `` `unauthenticated failure shows no banner`() ``.
    - `` `selecting a period shows its cached figures and refreshes it`() ``.
    - `` `a period without cached figures shows none while the overview stays`() ``.
    - `` `rapid period changes keep only the last refresh`() `` (gate refreshes; `SelectPeriod(Today)` then `SelectPeriod(ThisYear)` → the first job is cancelled, `isRefreshing` stays true until the last completes, `state.period == ThisYear`, `state.figures?.period == ThisYear`).
    - `` `cleared cache keeps the last content`() `` (content shown, then `overviewFlow.value = null` and the period's figures null → state keeps both).
    - `` `header shows the bootstrap title and user`() `` (title from `TestData` settings; falls back to `shop.displayName` when settings are null; `userName`, `roleName`).
    - `` `new sale needs sales-add`() `` (without the permission `canStartSale == false` and `NewSale` emits nothing; with it the effect `OpenNewSale` is emitted).
    - `` `tab and list choices are kept in state`() ``.
  - `MainViewModelTest`: `` `settings stay during the logout exit`() `` (settings shown; `settingsFlow.value = null` while logged in → still shown; state → `LoggedOut` → still shown); `` `a new login starts from the fresh cache`() `` (then `LoggedIn` again with the cache null → settings null).
  - `NavSavedStateTest`: the round trip uses the moved `DashboardKey`.
- [ ] **Step 2: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:common:testAndroidHostTest :feature:dashboard:testAndroidHostTest :composeApp:testAndroidHostTest` → compilation failures.
- [ ] **Step 3: Implement** per Interfaces. `DashboardScreen(state, onAction)` for now: `LoadingState` for `Loading`; `EmptyState` + Retry `PosButton` for `Error`; `EmptyState` for `Empty`; for `Ready` a plain column of the greeting, the four figures and `ErrorBanner` when `banner != null` (Tasks 6–7 replace it). Light and dark previews from `TestData`-like sample values local to the preview (feature code doesn't depend on `core:testing` outside tests).
- [ ] **Step 4: Run tests, builds, look at it.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64` → BUILD SUCCESSFUL. Emulator, Demo shop: Dashboard shows the demo figures for this month; tabs still work; process death (`adb shell am kill pk.paksoft.pos` with the app in the background, then reopen) returns to the same tab. Save `1b-t5-dashboard-plain.png`.
- [ ] **Step 5: Commit**

```bash
git add -A settings.gradle.kts feature/dashboard core/common core/testing core/designsystem composeApp docs/ARCHITECTURE.md
git commit -m "feat(dashboard): feature module, offline-first ViewModel and the Dashboard tab entry"
```

---

### Task 6: Dashboard UI, part 1: frame, figures, cash flow, FAB

**Files:**
- Create in `core/designsystem/src/commonMain/kotlin/pk/paksoft/pos/core/designsystem/`: `component/PosHeroCard.kt`, `component/PosSectionHeader.kt`, `component/PosExtendedFab.kt`, `component/PosPullToRefreshBox.kt`, `component/chart/BarChart.kt`
- Modify: `theme/PosMotion.kt` (`chartStartDelayMillis = 120`, `chartStaggerMillis = 35`), `theme/PosLayout.kt` (`fabClearance = 88.dp`), `component/Skeleton.kt` (while visible: `clearAndSetSemantics {}` and no pointer input reaching the content)
- Create: `core/ui/src/commonMain/kotlin/pk/paksoft/pos/core/ui/MoneyFormat.kt`, `MonthLabels.kt`; `core/ui/src/commonMain/composeResources/values/strings.xml` (month short and full names as `string-array`)
- Create in `feature/dashboard/.../`: `ui/DashboardHeader.kt`, `ui/PeriodChips.kt`, `ui/FiguresSection.kt`, `ui/CashFlowCard.kt`, `ui/DashboardSkeleton.kt`, `ui/FabScroll.kt`; rewrite `DashboardScreen.kt`; extend `strings.xml`
- Test: `core/ui/src/commonTest/.../MoneyFormatTest.kt`, `MonthLabelsTest.kt`; `core/designsystem/src/commonTest/.../component/chart/BarChartTest.kt`; `feature/dashboard/src/commonTest/.../FabScrollTest.kt`

**Interfaces:**
- Consumes: `DashboardUiState`, actions (Task 5); `PosFilterChip`, `ShopAvatar`, `ShopBrand.forShop`, `PosIconButton`, `animateCountAsState`, `enterStagger`, `SkeletonBox`, `ErrorBanner`, `posSharedBounds`, `newSaleKey()`.
- Produces (`core:ui`):
  - `fun formatMoneyWhole(money: Money, currency: Currency?): String`: half-up to whole rupees, `#,##0` grouping, symbol `Rs` for PKR or null currency, else the code; prefix `"Rs 1,234"` / suffix `"1,234 Rs"`; negatives `"-Rs 1,234"`.
  - `fun formatMoneyCompact(money: Money, currency: Currency?): String`: under 1,000 rupees as whole; under 10,000 one decimal `k` (`"Rs 4.5k"`, trailing `.0` dropped); under 1,000,000 whole `k` (`"Rs 482k"`); above, one decimal `M` (`"Rs 5.2M"`); half-up.
  - `fun monthIndex(yearMonth: String): Int?` (`"2026-06"` → 5; malformed → null); `@Composable fun monthShortName(yearMonth: String): String`, `monthFullName(yearMonth: String): String` (from the string arrays; malformed → the input).
- Produces (`core:designsystem`):
  - `@Composable fun PosHeroCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)`: `primary` surface, `onPrimary` content, 28 dp corners, a 190 dp `MaterialShapes.Cookie12Sided` at 10% `onPrimary` offset past the top-end corner, clipped.
  - `@Composable fun PosSectionHeader(title: String, modifier: Modifier = Modifier, trailing: (@Composable RowScope.() -> Unit)? = null)`: `titleMedium`, `semantics { heading() }`.
  - `@Composable fun PosExtendedFab(text: String, icon: ImageVector, expanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)`: M3 `ExtendedFloatingActionButton` on `tertiaryContainer`, `shapes.medium` (16 dp), `pressScale(PosPressScale.control)`, scales in on first composition with `fastSpatial` after `staggerDelayMillis(6, …)`; reduced → shown at once.
  - `@Composable fun PosPullToRefreshBox(isRefreshing: Boolean, onRefresh: () -> Unit, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit)`: `PullToRefreshBox` with the Expressive `PullToRefreshDefaults.LoadingIndicator`; if that indicator is not public in `1.12.0-alpha03`, use `PullToRefreshDefaults.Indicator` and say so in the report.
  - `@Composable fun PosGroupedBarChart(groups: List<List<Long>>, seriesColors: List<Color>, labels: List<String>, contentDescription: String, modifier: Modifier = Modifier, peakLabel: String? = null)`: Canvas; bars of a group side by side, rounded tops; heights from `barFractions(groups.flatten())`; each bar animates its height with `spatial` after `barDelayMillis(index, motion)` and retargets from its current height when data changes; `peakLabel` drawn above the last group's tallest bar, fading in with `effects`; `Modifier.semantics { this.contentDescription = contentDescription }`; reduced → final heights at once.
  - `internal fun barFractions(values: List<Long>): List<Float>` and `internal fun barDelayMillis(index: Int, startMillis: Int, stepMillis: Int, reduced: Boolean): Int`.
- Produces (`feature:dashboard`): `internal fun fabExpandedAfterScroll(previous: Int, current: Int, wasExpanded: Boolean): Boolean`.

- [ ] **Step 1: Write the failing tests.**
  - `MoneyFormatTest`: `formatMoneyWhole(Money(48_235_000), null) == "Rs 482,350"`; `Money(1_234_550)` → `"Rs 12,346"` (half-up); `Money(-120_000)` → `"-Rs 1,200"`; `Money(0)` → `"Rs 0"`; crore `Money(10_000_000_000)` → `"Rs 100,000,000"`; suffix currency `Currency(…, code = "PKR", position = Suffix)` → `"482,350 Rs"`; `code = "USD"` prefix → `"USD 482,350"`. `formatMoneyCompact`: `Money(95_000)` → `"Rs 950"`, `Money(450_000)` → `"Rs 4.5k"`, `Money(400_000)` → `"Rs 4k"`, `Money(48_235_000)` → `"Rs 482k"`, `Money(518_490_000)` → `"Rs 5.2M"`, `Money(-450_000)` → `"-Rs 4.5k"`.
  - `MonthLabelsTest`: `monthIndex("2026-06") == 5`, `monthIndex("2026-13") == null`, `monthIndex("x") == null`.
  - `BarChartTest`: `barFractions(listOf(50, 100, 0)) == listOf(0.5f, 1f, 0f)`; all zero → all `0f`; empty → empty; a negative value → `0f`; `barDelayMillis(0, 120, 35, false) == 120`, `barDelayMillis(3, 120, 35, false) == 225`, `barDelayMillis(3, 120, 35, true) == 0`.
  - `FabScrollTest`: `(0, 0, false)` → true (at top); `(100, 160, true)` → false (down); `(160, 100, false)` → true (up); `(100, 100, false)` → false (unchanged).
- [ ] **Step 2: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:ui:testAndroidHostTest :core:designsystem:testAndroidHostTest :feature:dashboard:testAndroidHostTest` → compilation failures.
- [ ] **Step 3: Implement the designsystem pieces and formatters** per Interfaces, each new component with light and dark previews.
- [ ] **Step 4: Build the screen frame and part-1 sections** (look per the spec and the preview's `#s-home`):
  - `DashboardScreen`: `Box { PosPullToRefreshBox(isRefreshing, onRefresh = Refresh) { Column(verticalScroll(scrollState), widthIn(max = PosLayout.contentMaxWidth), bottom padding PosLayout.fabClearance) { … } }; PosExtendedFab(Alignment.BottomEnd, PosSpacing.l padding, posSharedBounds(newSaleKey(), PosSharedResize.Remeasure)) when canStartSale }`; `expanded` follows `fabExpandedAfterScroll` on `scrollState.value` changes.
  - Header (index 0): `ShopAvatar(ShopBrand.forShop(shopId, shopDisplayName), 40.dp)`, `shopTitle` (titleSmall), "{user} · {role}" (bodySmall, onSurfaceVariant), `PosIconButton(PosIcons.Refresh, "Refresh dashboard")` → `Refresh`. Greeting (1): `headlineMedium` "Good morning, {user}" (three strings). Chips (2): horizontally scrolling `PosFilterChip`s "Today", "7 days", "This month", "This year".
  - Banner: `ErrorBanner(message, onRetry = Refresh)` above the hero when `banner != null` and content is `Ready`/`Empty`.
  - Hero (3): `PosHeroCard` with "Revenue · today|last 7 days|this month|this year" (labelLarge), revenue counting with `animateCountAsState(figures.revenue.paisa)` formatted by `formatMoneyWhole` in `moneyLarge`, and a pill (onPrimary at 14% alpha, full round) "Profit {count-up profit}". Tiles (4): two equal tiles on `surfaceContainerLow`, `shapes.large`: "Sale returns", "Purchase returns", `moneyMedium` count-ups. When `figures == null`: `SkeletonBox`es in place of the three values.
  - Cash flow (5): card on `surfaceContainerLow`, `PosSectionHeader("Cash flow")` with a legend (Received = `chartIn`, Paid out = `chartOut`), `PosGroupedBarChart` 118 dp high, month short names, `peakLabel = formatMoneyCompact(last month received)`, description "Cash flow, last 7 months. {Month}: received {Rs}, paid out {Rs}. …".
  - `Loading` → `DashboardSkeleton` (header, hero, tiles, a chart card, three rows of `SkeletonBox`es) inside a container with `semantics { contentDescription = "Loading dashboard" }`; `Error` and `Empty` as in Task 5, restyled with `EmptyState` (`PosIcons.CloudOff` / `PosIcons.Sales`) and the copy "Couldn't load the dashboard" / "No sales yet" + "Sales, purchases and payments show up here once the shop starts trading."
  - Sections carry `Modifier.enterStagger(index)`.
  - Previews: Ready, Loading, Error, Empty, light and dark, and a Ready preview with a negative profit.
- [ ] **Step 5: Run tests, builds, look at it.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64` → BUILD SUCCESSFUL. Emulator, Demo shop, light and dark: figures count up, bars grow staggered, chips switch the four figures only, pull-to-refresh shows the Expressive indicator, the FAB collapses on scroll down and expands on scroll up, New sale grows into the POS tab placeholder and Back shrinks it into the FAB; tapping the POS tab still fades through. The Demo shop answers in-app and can't go offline, so Loading, Error and Empty are checked in their previews. Reduced motion (`adb shell settings put global animator_duration_scale 0`, then restore to 1): every value and bar shows at once and the FAB → POS change is a plain fade through. Save `1b-t6-dashboard-light.png`, `1b-t6-dashboard-dark.png`, `1b-t6-period-year.png`, `1b-t6-fab-collapsed.png`, `1b-t6-fab-to-pos.png` (mid-transform if it can be caught, else the POS screen), `1b-t6-reduced.png`.
- [ ] **Step 6: Commit**

```bash
git add -A core/designsystem core/ui feature/dashboard
git commit -m "feat(dashboard): greeting, period chips, counting figures, cash-flow bars, pull to refresh and the New sale FAB"
```

---

### Task 7: Dashboard UI, part 2: donut, yearly, recent, best sellers, docs

**Files:**
- Create in `core/designsystem/.../component/`: `chart/DonutChart.kt`, `PosSegmentedTabs.kt`, `StatusBadge.kt`
- Create: `core/ui/src/commonMain/kotlin/pk/paksoft/pos/core/ui/ShopDateFormat.kt`, `QuantityFormat.kt`
- Create in `feature/dashboard/.../ui/`: `MonthBreakdownCard.kt`, `YearlyCard.kt`, `RecentCard.kt`, `RecentRows.kt` (row models and status tones), `BestSellersCard.kt`; modify `DashboardScreen.kt`, `strings.xml`
- Modify: `feature/settings/.../MoreUiState.kt`, `MoreViewModel.kt`, `MoreScreen.kt`, `MoreViewModelTest.kt` (rename `shopName` → `shopTitle`)
- Test: `core/designsystem/src/commonTest/.../component/chart/DonutChartTest.kt`; `core/ui/src/commonTest/.../ShopDateFormatTest.kt`, `QuantityFormatTest.kt`; `feature/dashboard/src/commonTest/.../RecentRowsTest.kt`
- Docs: `docs/ARCHITECTURE.md` §12 (new components; remove `StatusBadge` and `KpiTile` from "still to build"), §13 (money and date formatters as built), `README.md` (status: Dashboard done on the Demo shop; Works/Checked lines)

**Interfaces:**
- Consumes: Tasks 5–6 (`DashboardUiState.recentTab`, `bestSellerList`, actions; `PosSectionHeader`, `PosGroupedBarChart`, `formatMoneyWhole`, `monthShortName`, `monthFullName`).
- Produces (`core:designsystem`):
  - `@Composable fun PosDonutChart(values: List<Long>, colors: List<Color>, contentDescription: String, modifier: Modifier = Modifier, centerLabel: String? = null)`: ring on `surfaceContainerHigh`, arcs from `donutSweeps`, total sweep animates 0 → 1 with `slowSpatial` (reduced → 1), semantics description.
  - `internal data class DonutArc(val startAngle: Float, val sweep: Float)`; `internal fun donutSweeps(values: List<Long>, gapDegrees: Float = 2f): List<DonutArc>` (start at −90°; non-zero slices share `360 − gaps`, one gap after each non-zero slice; a single non-zero slice is a full 360° with no gap; zero slices get no arc; all zero → empty).
  - `@Composable fun PosSegmentedTabs(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier)`: M3 `SingleChoiceSegmentedButtonRow`, no check icon, `labelMedium`, one line with ellipsis, `pressScale`.
  - `enum class StatusTone { Positive, Warning, Negative, Neutral }`; `@Composable fun StatusBadge(text: String, tone: StatusTone, modifier: Modifier = Modifier)`: Positive `paidContainer`/`onPaidContainer`, Warning `dueContainer`/`onDueContainer`, Negative `errorContainer`/`onErrorContainer`, Neutral `surfaceContainerHigh`/`onSurfaceVariant`; `labelSmall`, `shapes.small`.
- Produces (`core:ui`): `fun formatShopDate(isoDate: String, phpFormat: String): String` (tokens `d` `j` `m` `n` `Y` `y` `M` (`Jan`); other characters copied; malformed date → input unchanged); `fun formatQuantity(qty: Double): String` (up to 2 decimals, trailing zeros dropped).
- Produces (`feature:dashboard`): `internal data class RecentRow(val id: Long, val reference: String, val party: String?, val date: String, val amount: Money, val badge: StringResource, val tone: StatusTone)` built by `internal fun RecentTransactions.rows(tab: RecentTab): List<RecentRow>` (payments: `party = null`, badge = the method's name; the composable shows a null party of a sale, purchase or quotation as `dashboard_no_party` = "N/A", as the web) and `internal fun SaleStatus.tone()`, `PurchaseStatus.tone()`, `QuotationStatus.tone()` (web colours: sale Completed Positive, Pending Negative, Draft Warning; purchase Received and Partial Positive, Pending and Ordered Negative; quotation Pending Negative, Sent Positive; `Unknown` Neutral; payments always Neutral with the method's name).

- [ ] **Step 1: Write the failing tests.**
  - `DonutChartTest`: `donutSweeps(listOf(1, 1, 2))` == `[DonutArc(-90f, 88.5f), DonutArc(0.5f, 88.5f), DonutArc(91f, 177f)]`; `donutSweeps(listOf(0, 5, 0)) == [DonutArc(-90f, 360f)]`; all zero → empty; empty → empty.
  - `ShopDateFormatTest`: `("2026-11-08", "d/m/Y") == "08/11/2026"`; `("2026-11-08", "j-n-y") == "8-11-26"`; `("2026-11-08", "d M Y") == "08 Nov 2026"`; `("bad", "d/m/Y") == "bad"`.
  - `QuantityFormatTest`: `2.0 → "2"`, `2.5 → "2.5"`, `1.256 → "1.26"`, `0.0 → "0"`.
  - `RecentRowsTest` (commonTest, asserts `StringResource` ids, not text): tones per the mapping above; `rows(Purchases)` keeps a `null` supplier as `party == null`; `rows(Payments)` gives `badge == Res.string.payment_gift_card` for `GiftCard`, `Res.string.status_unknown` for `Unknown`, and `tone == Neutral`; rows keep server order and never exceed 5.
- [ ] **Step 2: Run and see them fail.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew :core:designsystem:testAndroidHostTest :core:ui:testAndroidHostTest :feature:dashboard:iosSimulatorArm64Test` → compilation failures.
- [ ] **Step 3: Implement the components and formatters** per Interfaces, with light and dark previews.
- [ ] **Step 4: Build the part-2 sections** (each card on `surfaceContainerLow`, `shapes.extraLarge`, `PosSectionHeader`, `enterStagger` 6, 7, 8, 8):
  - This month (6): "This month · {November 2026}", `PosDonutChart(purchase, revenue, expense)` colours `chartOut`, `chartIn`, `secondary`, a legend with the three amounts (`formatMoneyWhole`).
  - This year (7): "{2026}: sold and purchased", `PosGroupedBarChart` of 12 months (sold `chartIn`, purchased `chartOut`), single-letter month labels, `peakLabel` = compact sold of the current month.
  - Recent (8): "Recent transactions", `PosSegmentedTabs` Sales · Purchases · Quotations · Payments → `SelectRecentTab`; up to 5 `RecentRow`s: reference (titleSmall), "{party} · {date via formatShopDate(date, dateFormat)}" (bodySmall), amount (`moneySmall`, `formatMoneyWhole`) and `StatusBadge`; an empty tab shows "Nothing yet" (bodyMedium, onSurfaceVariant). Tab content swaps with `AnimatedContent` fading through `PosTransitions.fadeThrough(motion)`.
  - Best sellers (8): "Best sellers" with a "Top 5" `StatusBadge(Neutral)`, `PosSegmentedTabs` This month · This year · By amount → `SelectBestSellerList`; a caption "By quantity sold in {November 2026}" / "By quantity sold in {2026}" / "By sales amount in {2026}"; rows: rank in a 28 dp circle (`secondaryContainer`), name (bodyLarge), `[code]` (bodySmall), trailing "{qty} sold" (`formatQuantity`) or amount.
  - `MoreUiState.shopName` → `shopTitle` everywhere (state, ViewModel, screen, tests).
- [ ] **Step 5: Run tests, builds, look at it on both platforms.** `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew allTests :androidApp:assembleDebug :composeApp:linkDebugFrameworkIosSimulatorArm64` and the `xcodebuild` command from `CLAUDE.md` (expect `** BUILD SUCCEEDED **`). Emulator, Demo shop, light and dark: scroll the whole Dashboard; the donut sweeps in; yearly bars grow; each recent tab and best-seller list switches; set `adb shell settings put system font_scale 1.3` and check the segmented tabs (labels ellipsize, nothing overlaps), then restore `1.0`; pull to refresh on the Dashboard, then More → Log out: no crash, a clean login, and after logging in again the Dashboard shows the skeleton then fresh data (no previous session's content). iOS simulator: launch with the seeded Demo shop and confirm the build renders the login (tap-through still unavailable, as in Phase 1a; report it). Save `1b-t7-month-yearly.png`, `1b-t7-recent-purchases.png`, `1b-t7-best-sellers.png`, `1b-t7-dark.png`, `1b-t7-font-1_3.png`.
- [ ] **Step 6: Commit**

```bash
git add -A core/designsystem core/ui feature/dashboard feature/settings docs/ARCHITECTURE.md README.md
git commit -m "feat(dashboard): month donut, yearly bars, recent transactions and best sellers; docs"
```

---

## After the last task

Whole-branch review against the spec. Update `README.md` "Not verified yet" with anything the review or the iOS limits leave unchecked (iOS tap-through to the Dashboard, the FAB transform on iOS). Record the remaining Phase 1a carry-overs (tab tap from a pushed detail slides; `RollingText` retarget pause) in the ledger for the Sales and POS plans.
