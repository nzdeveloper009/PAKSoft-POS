# Domain reference: how the existing POS behaves

This file describes the business rules of the existing web system so the app (and the new API) behave **exactly** the same way. Every rule names the legacy source it came from. When this file and the legacy code disagree, the legacy code wins: fix this file.

Legacy source root (read-only reference, same machine):
`../pbsf.org.pk/smf/` (Laravel 6.20, PHP 7.4). Paths below are relative to it.

---

## 1. Deployments ("shops")

One codebase, three deployments, one MySQL database each. The app treats each as a **shop** the user picks before logging in. These three are the **only** things the app covers: the WordPress site at the `pbsf.org.pk` root (Pakistan Blind Sports Federation) and anything under `wp-*` are out of scope.

| Shop id | Display name | Web base URL | API base URL (to be added) | Notes |
|---|---|---|---|---|
| `smf` | Super Madina Solar Fan | `https://pbsf.org.pk/smf` | `https://pbsf.org.pk/smf/api/v1` | Reference deployment. Has Ledger. |
| `bsf` | BSF | `https://pbsf.org.pk/bsf` | `https://pbsf.org.pk/bsf/api/v1` | `app/` is byte-identical to `smf`. |
| `mummyandme` | Mummy & Me | `https://pos.mummyandme.com.pk` | `https://pos.mummyandme.com.pk/api/v1` | Older copy: **no Ledger** module, some controller differences. Must be brought in line with `smf` before the API is deployed there. |

The web root of each Laravel app is the project root (not `public/`), so `index.php` sits next to `app/`. API routes therefore live at `<base>/api/...`.

## 2. Users, roles, permissions

Source: `app/User.php`, `app/Http/Middleware/Active.php`, `app/Http/Controllers/Auth/LoginController.php`, `spatie/laravel-permission`.

- **Login field:** the single "name" input accepts a **username or an email**. The server checks `filter_var(input, FILTER_VALIDATE_EMAIL)` and authenticates on `email` or `name` accordingly.
- **Active check:** a user with `is_active = 0` can authenticate but every module redirects to `/dashboard`. The API must reject inactive users at login with `403 account_inactive`.
- **Roles** (`roles` table, ids are stable):

| role_id | Name | Meaning |
|---|---|---|
| 1 | Admin | Everything. Sees Role Permission screen. |
| 2 | Owner | Everything. Sees Role Permission screen. |
| 4 | staff | Only what the role's permissions allow. |
| 5 | Customer | Customer portal (own sales, payments, quotations, returns). **Out of scope for the app**; login must return `403 role_not_supported`. |

- **Staff data scope:** `general_settings.staff_access` is `all` or `own`. When it is `own` **and** `role_id > 2`, list queries (sales, purchases, recent POS sales/drafts, dashboard figures) are limited to rows where `user_id = current user`. The API must apply this server-side.
- **Permissions** are named strings checked with `$role->hasPermissionTo('<name>')`. The app receives the list at login and hides what the user can't do; the server still enforces every check. Full list:

```
products-index products-add products-edit products-delete
purchases-index purchases-add purchases-edit purchases-delete
sales-index sales-add sales-edit sales-delete
quotes-index quotes-add quotes-edit quotes-delete
transfers-index transfers-add transfers-edit transfers-delete
returns-index returns-add returns-edit returns-delete
purchase-return-index purchase-return-add purchase-return-edit purchase-return-delete
customers-index customers-add customers-edit customers-delete
suppliers-index suppliers-add suppliers-edit suppliers-delete
billers-index billers-add billers-edit billers-delete
users-index users-add users-edit users-delete
employees-index employees-add employees-edit employees-delete
expenses-index expenses-add expenses-edit expenses-delete
product-report purchase-report sale-report customer-report due-report supplier-report user-report
profit-loss best-seller daily-sale monthly-sale daily-purchase monthly-purchase payment-report
warehouse-report warehouse-stock-report product-qty-alert
account-index balance-sheet account-statement money-transfer
department attendance payroll holiday
general_setting mail_setting pos_setting hrm_setting sms_setting create_sms
category unit tax brand warehouse customer_group currency coupon gift_card delivery
stock_count adjustment print_barcode
send_notification today_sale today_profit backup_database empty_database
```

- **Ledger permissions** reuse sale permissions: list/view = `sales-index`, create/import = `sales-add`, edit/rename/save = `sales-edit`, delete = `sales-delete` (`LedgerSheetController::allowed`).

## 3. Codes and enums

| Field | Value → meaning | Source |
|---|---|---|
| `sales.sale_status` | `1` Completed · `2` Pending · `3` Draft | `SaleController::saleData` |
| `sales.payment_status` | `1` Pending · `2` Due · `3` Partial · `4` Paid | `SaleController::saleData` |
| POS `payment_status` on create | `4` (Paid) if `grand_total - paid_amount == 0`, else `2` (Due) | `SaleController::store` |
| `paid_by_id` (form) → `payments.paying_method` | `1` Cash · `2` Gift Card · `3` Credit Card · `4` Cheque · `5` Paypal · `6` Deposit | `SaleController::store` |
| `products.type` | `standard` · `combo` · `digital` · `service` | `products` table |
| `products.tax_method` | `1` Exclusive (tax added on top) · `2` Inclusive (price contains tax) | `pos.blade.php` `calculateRowProductData` |
| `units.operator` | `*` or `/` with `operation_value`, relative to `base_unit` | `units` table |
| `coupons.type` | `fixed` (amount off) · `percentage` | `pos.blade.php` `couponDiscount` |
| `general_settings.currency_position` | `prefix` · `suffix` | settings |
| `general_settings.date_format` | PHP format, e.g. `d/m/Y` | settings |
| `cash_registers.status` | `1` open · `0` closed | `CashRegisterController` |
| `ledger_sheets.folder` | `sales` · `purchase` | `app/LedgerSheet.php` |

API rule: the API sends these as **lowercase string enums** (`"completed"`, `"draft"`, `"due"`, `"cash"`…), never raw integers. Mapping lives in one place on the server (`App\Http\Resources\Api\V1\Enums`) and one place in the app (`core:model`).

## 4. The sellable item (what one POS grid tile / cart line represents)

Source: `SaleController::posSale`, `::getProduct`, `::limsProductSearch`; `pos.blade.php` `addNewProduct`.

A product becomes **one or more sellable items**:

- A product **without variants** → one item, `code = products.code`.
- A product **with variants** (`is_variant = 1`) → one item per row in `product_variants`, name `"<product name> [<variant name>]"`, `code = product_variants.item_code`, price `+= product_variants.additional_price`.
- A product **with batches** (`is_batch = 1`) → the cashier picks a batch (`product_batches.batch_no`, with `expired_date`); stock is checked per batch.
- **POS grid** shows only `is_active = 1` **and** `featured = 1` products (`scopeActiveFeatured`), paged by `pos_setting.product_number`. Search and barcode lookup cover all active products.
- **Barcode / search lookup:** the input is matched first against `products.code`, then against `product_variants.item_code`. Anything after `" ("` in the typed text is ignored (the web shows `CODE (Name)` suggestions).

### Unit price of an item, before cart math

Apply in this order (source: `limsProductSearch` + `addNewProduct`):

1. `base = promotion_price` if `promotion = 1` **and** today ≤ `last_date`; otherwise `price`.
2. If the item is a variant: `base += additional_price`.
3. If the product has a per-warehouse price (`is_diffPrice = 1` and `product_warehouse.price` is set for the selected warehouse): `base = product_warehouse.price`.
4. Currency: `base *= currency.exchange_rate` (the currency selected in general settings; `1` for the home currency).
5. Customer group markup: `unit_price = base + base * (customer_group.percentage / 100)`. Changing the customer on the POS re-prices every line.
6. **Sale unit** (`standard` products only): the item's unit list is the product's base unit plus every unit whose `base_unit` is that unit; the product's `sale_unit_id` comes first. For the chosen unit:
   - operator `*` → `unit_price = unit_price * operation_value`
   - operator `/` → `unit_price = unit_price / operation_value`
   - non-standard products have no unit (`n/a`).
7. The cashier can override the line price by hand (`applyInlinePrice`). The override replaces `unit_price` for that line.

### Stock availability

- Only `product_warehouse.qty > 0` rows for the selected warehouse are sellable for `standard` items (`getProduct`). `combo`, `digital` and `service` items are always listed.
- `checkQuantity` blocks adding more than the warehouse stock (converted to the sale unit) for standard items. Combo items check every child product.
- The app shows stock but **the server is the authority**; the server re-checks on submit and returns `422 insufficient_stock` with the offending lines.

### POS defaults and who can change them

Source: `pos.blade.php` (around lines 1853 and 2127–2160).

- **Staff (role_id > 2):** the warehouse and biller pickers are **hidden** and fixed to the user's own `users.warehouse_id` and `users.biller_id`. On opening the POS, if the user has no open cash register for that warehouse, the "open register" dialog is shown.
- **Admin / Owner (role_id ≤ 2):** warehouse and biller are selectable; the POS starts with the last used values (remembered on the device), else `pos_setting.warehouse_id` / `biller_id`.
- **Customer:** last used, else `pos_setting.customer_id` (normally "Walk in Customer").
- A customer and a warehouse must be selected before products can be added.
- The API enforces the staff lock: a staff sale for another warehouse or biller is rejected.

## 5. Cart math (must match `pos.blade.php` exactly)

Source: `calculateRowProductData`, `calculateTotal`, `calculateGrandTotal`, `couponDiscount` in `resources/views/sale/pos.blade.php`.

Per line, with `q` = quantity, `p` = unit price after section 4, `d` = per-unit discount, `r` = tax rate %:

```
if tax_method == EXCLUSIVE (1):
    net_unit_price = p - d
    tax            = net_unit_price * q * r / 100
    subtotal       = net_unit_price * q + tax
else (INCLUSIVE, 2):
    unit_incl      = p - d
    net_unit_price = unit_incl * 100 / (100 + r)
    tax            = (unit_incl - net_unit_price) * q
    subtotal       = unit_incl * q
line_discount = d * q
```

Order level:

```
item            = number of lines
total_qty       = Σ q
total_discount  = Σ line_discount
total_tax       = Σ tax
total_price     = Σ subtotal
order_tax       = (total_price - order_discount) * order_tax_rate / 100
grand_total     = total_price + order_tax + shipping_cost - order_discount
coupon:
    fixed:      if grand_total >= minimum_amount → coupon_discount = amount
    percentage: coupon_discount = grand_total * amount / 100
grand_total    -= coupon_discount
```

- `order_discount` and `shipping_cost` are amounts typed by the cashier; `order_tax_rate` comes from the `taxes` list (or 0).
- Coupon validity: `used < quantity` and today ≤ `expired_date`; `is_active = 1`.
- **Previous balance** (custom addition): a positive amount typed by the cashier. It is **not** added to `grand_total`. It is stored in `sales.previous_balance` (column added by hand on the live server; may be missing on other shops; check with `Schema::hasColumn`) and printed on the receipt as `Payable = grand_total + previous_balance`.
- **Rounding:** the web rounds every displayed and submitted figure with `toFixed(2)` at each step above. The app does the same at the same steps (half-up to 2 decimals) so totals match to the paisa. The server accepts a difference of at most `0.01` per figure.
- **Change:** `change = paying_amount (cash tendered) - paid_amount`.

## 6. Creating a sale (what the server does)

Source: `SaleController::store` (lines ~341–700).

1. `reference_no`: given by the cashier, else `posr-YYYYMMDD-hhmmss` (POS) or `sr-YYYYMMDD-hhmmss` (Add Sale). Must be unique in `sales`.
2. Attaches the user's **open cash register** for that warehouse, if any (`cash_registers` with `status = 1`).
3. If the sale finalizes a **draft** (`draft = 1`, `sale_id` = draft id), the draft and its lines are deleted first.
4. If a coupon is used, `coupons.used += 1`.
5. Creates `sales` row, then one `product_sales` row per line.
6. **Custom item:** a line whose `product_id` is `custom:<url-encoded name>` gets a hidden service product created on first use (`customProductId`: code `CUS-` + first 8 hex chars of `md5(lowercase name)`, `type = service`, `is_active = 0`, price = line price). Same name → same product.
7. **Stock** (only when `sale_status = 1` Completed): for each standard line, convert `qty` to base units with the sale unit's operator, then subtract from `products.qty`, from the variant row (if any) or the batch row (if any), and from `product_warehouse` for that warehouse. Combo lines subtract `qty * child_qty` from each child product and its warehouse row.
8. Sends a "Sale Details" email to the customer if they have an email and the sale is completed (failures are swallowed).
9. **Payment** is recorded when `payment_status` is 3 or 4, or when a POS sale is Due but `paid_amount > 0`: one `payments` row (`payment_reference = spr-YYYYMMDD-hhmmss`, `account_id` = the default account, `amount = paid_amount`, `change = paying_amount - paid_amount`), plus a method row in `payment_with_cheque` / `payment_with_gift_card` (and gift card `expense += paid_amount`) / `payment_with_credit_card` (Stripe charge) / PayPal redirect.
10. POS then redirects to the invoice page `sales/gen_invoice/{id}`.

Drafts (`sale_status = 3`) do **not** touch stock and record no payment.

**App scope for payments:** Cash, Cheque, Gift Card and Deposit. Credit Card (Stripe token in the browser) and PayPal (browser redirect) are **not** supported in the app at first.

## 7. Receipt (invoice) contents

Source: `resources/views/sale/invoice.blade.php`, `SaleController::genInvoice`.

The invoice has been customized on the server. Header: shop logo and company name, biller details, date, reference no, customer. Lines: name, qty × unit price, subtotal. Payments table: paying method, amount, change. Sale note.

Totals block, in this order, each row shown only when its value is non-zero:

```
Subtotal                      total_price
Item Discount (included)      Σ line discount
IGST | SGST + CGST            only when invoice_format == "gst" (state 1 → IGST; state 2 → half SGST, half CGST)
Order Tax                     order_tax
Order Discount              - order_discount
Coupon Discount             - coupon_discount
Shipping Cost                 shipping_cost
Grand Total (<currency code>) grand_total
Previous Balance            + previous_balance            (only if > 0.009)
Total Payable                 grand_total + previous_balance
Paid                          paid_amount
New Balance | Balance Due     total_payable - paid_amount (only if > 0.009; label is "New Balance" when there is a previous balance)
```

Footer: a Code 128 barcode **and** a QR code of the reference no. Numbers are formatted `#,##0.00`. The amount-in-words helper (`kwn/number-to-words`) is used by `genInvoice`. The app renders this natively and can share it as PDF.

## 8. Cash register

Source: `CashRegisterController`.

- One open register per user per warehouse. On entering the POS, `check-availability/{warehouse_id}` runs; if none is open, a dialog asks for **cash in hand** to open one (Admin/Owner also pick the warehouse there). The dialog can be dismissed: **selling is not blocked** without a register; the sale simply isn't attached to one.
- `getDetails/{id}` returns: cash in hand, total sale amount, total payment, cash/cheque/gift card/credit card/deposit/PayPal payments, total sale return, total expense, total cash. Closing sets `status = 0`.

## 9. Dashboard figures

Source: `HomeController::index`, `::dashboardFilter`.

- Period filter: today, last 7 days, this month (default), this year.
- **Revenue** = Σ `product_sales.total` in the period (own sales only for staff with `own` access).
- **Profit** = revenue − cost, where cost is computed FIFO-style from `product_purchases` for each product/batch sold (see `HomeController` loop). Port this to the API as-is; the app only displays it.
- **Sale return**, **Purchase return** = Σ grand totals of returns in the period.
- **Cash flow** chart: payments received vs payments sent per month for the last months; **donut**: purchase vs revenue vs expense for the current month.

## 10. Ledger sheets (custom module)

Source: `app/LedgerSheet.php`, `app/LedgerSheetImporter.php`, `app/Http/Controllers/LedgerSheetController.php`, `resources/views/ledger_sheets/*`. Routes: `routes/ledger_sheets.php`.

- Two folders: `sales` ("Sales Ledgers"), `purchase` ("Purchase Ledgers"). Files are listed by name.
- A file is a **grid of text cells** stored as JSON (`ledger_sheets.data`, an array of rows, each an array of strings). Row 0 is the heading row (shown bold). No formulas: Balance is a plain text column the user types into.
- New file default header:
  - sales: `Date | Customer / Details | Qty | Rate | Amount | Received | Balance`
  - purchase: `Date | Supplier / Details | Qty | Rate | Amount | Paid | Balance`
  - plus 15 empty rows.
- Limits: min 7 columns (on import), max **40** columns, max **5000** rows, max **1000** characters per cell. `cleanGrid` pads every row to the same width and drops non-array rows.
- `rows_count` = rows (excluding header) with at least one non-blank cell; `cols_count` = width.
- **Autosave:** the editor posts the whole grid as JSON; the server replies `{ok, saved_at, rows, cols}`. Last write wins on the web.
- Import: `.csv` or `.xlsx` (Google Sheets export). Every sheet tab becomes one file in the chosen folder; single-sheet imports take the file name.
- Print: an A4 printable page (browser "Save as PDF").
- **Not reachable:** `LedgerFileController` + `routes/ledger.php` (typed columns with running balance, `ledger_files/columns/rows` tables). `routes/web.php` never requires `ledger.php`. Ignore it unless the owner asks for it.

## 11. Database quick reference (core tables for Phase 1)

Types follow MySQL in the 2021 SalePro demo dump (**not** live data). Money columns are `double` (and `products.price/cost` are `varchar`!). The API normalizes all of them.

| Table | Key columns |
|---|---|
| `users` | id, name (username), email, password, phone, company_name, role_id, biller_id, warehouse_id, is_active, is_deleted |
| `products` | id, name, code, type, barcode_symbology, brand_id, category_id, unit_id, purchase_unit_id, sale_unit_id, cost, price, qty, alert_quantity, promotion, promotion_price, starting_date, last_date, tax_id, tax_method, image (comma-separated file names), is_variant, is_batch, is_diffPrice, featured, product_list/qty_list/price_list (combo), product_details, is_active |
| `product_warehouse` | product_id, product_batch_id, variant_id, warehouse_id, qty, price |
| `product_variants` | product_id, variant_id, position, item_code, additional_price, qty |
| `variants` | id, name |
| `product_batches` | product_id, batch_no, expired_date, qty |
| `sales` | id, reference_no, user_id, cash_register_id, customer_id, warehouse_id, biller_id, item, total_qty, total_discount, total_tax, total_price, grand_total, order_tax_rate, order_tax, order_discount, coupon_id, coupon_discount, shipping_cost, sale_status, payment_status, document, paid_amount, sale_note, staff_note, (previous_balance), created_at |
| `product_sales` | sale_id, product_id, product_batch_id, variant_id, qty, sale_unit_id, net_unit_price, discount, tax_rate, tax, total |
| `payments` | payment_reference, user_id, purchase_id, sale_id, cash_register_id, account_id, amount, change, paying_method, payment_note |
| `payment_with_cheque` / `payment_with_gift_card` | payment_id, cheque_no / gift_card_id |
| `customers` | id, customer_group_id, user_id, name, company_name, email, phone_number, tax_no, address, city, state, postal_code, country, deposit, expense, is_active |
| `customer_groups` | id, name, percentage, is_active |
| `deposits` | amount, customer_id, user_id, note |
| `warehouses` | id, name, phone, email, address, is_active |
| `billers` | id, name, image, company_name, vat_number, email, phone_number, address, city, state, postal_code, country, is_active |
| `categories` | id, name, image, parent_id, is_active |
| `brands` | id, title, image, is_active |
| `units` | id, unit_code, unit_name, base_unit, operator, operation_value, is_active |
| `taxes` | id, name, rate, is_active |
| `coupons` | id, code, type, amount, minimum_amount, quantity, used, expired_date, is_active |
| `gift_cards` | id, card_no, amount, expense, customer_id, expired_date, is_active (balance = amount − expense) |
| `accounts` | id, account_no, name, initial_balance, total_balance, is_default, is_active |
| `cash_registers` | id, cash_in_hand, user_id, warehouse_id, status |
| `pos_setting` | customer_id, warehouse_id, biller_id (POS defaults), product_number (grid page size), keybord_active, stripe keys (**never expose**) |
| `general_settings` | site_title, site_logo, currency (→ `currencies.id`), staff_access, date_format, developed_by, invoice_format, theme, currency_position |
| `currencies` | id, name, code, exchange_rate |
| `ledger_sheets` | id, folder, name, data (JSON grid), rows_count, cols_count, timestamps. Created by hand on the server; no migration exists yet. |

Image URLs: `<shop base>/public/images/product/<file>` (first file of `products.image`), `/public/images/category/`, `/public/images/brand/`, logo `/public/logo/<site_logo>`. `zummXD2dvAtI.png` is the "no image" placeholder.

## 12. Legacy source map

| Topic | File |
|---|---|
| All web routes | `routes/web.php`, `routes/ledger_sheets.php` |
| POS screen (UI + all cart JS) | `resources/views/sale/pos.blade.php` (3,423 lines) |
| Sale create/update/payments | `app/Http/Controllers/SaleController.php` (2,544 lines) |
| Dashboard | `app/Http/Controllers/HomeController.php`, `resources/views/index.blade.php` |
| Products | `app/Http/Controllers/ProductController.php`, `resources/views/product/` |
| Customers & deposits | `app/Http/Controllers/CustomerController.php` |
| Cash register | `app/Http/Controllers/CashRegisterController.php` |
| Ledger | `app/LedgerSheet.php`, `app/LedgerSheetImporter.php`, `app/Http/Controllers/LedgerSheetController.php`, `resources/views/ledger_sheets/` |
| Reports | `app/Http/Controllers/ReportController.php` (1,619 lines) |
| Sidebar (what each role sees) | `resources/views/layout/main.blade.php` |
| Theme colors | `public/css/custom-default.css` |
| Translations (15 languages) | `resources/lang/<code>/file.php` |
