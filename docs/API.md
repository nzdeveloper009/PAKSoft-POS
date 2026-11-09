# API v1: contract between the app and the Laravel backend

**Status:** design. Nothing in this file exists on the server yet. The existing web app has no JSON API (`routes/api.php` is empty).

This file is the single source of truth for requests and responses. **Change it first**, then the PHP side, then the Kotlin side. The Kotlin DTOs in `core:network` and the PHP API Resources must match the examples here; JSON examples double as test fixtures (see `CLAUDE.md` → Testing).

Business rules behind these endpoints: [`DOMAIN.md`](DOMAIN.md).

Example values (names, emails, ids, amounts) are illustrative, shaped like the live SMF data. They are not real records.

---

## Part 1 — Conventions

### 1.1 Base URL

`<shop base>/api/v1`, one per shop:

| Shop | Base |
|---|---|
| Super Madina Solar Fan | `https://pbsf.org.pk/smf/api/v1` |
| BSF | `https://pbsf.org.pk/bsf/api/v1` |
| Mummy & Me | `https://pos.mummyandme.com.pk/api/v1` |
| Staging (to be created, used for all development) | `https://pbsf.org.pk/smf-staging/api/v1` |

**Never run write requests against a production shop during development or tests.** Use staging or Ktor `MockEngine`.

### 1.2 Headers

| Header | Value | When |
|---|---|---|
| `Accept` | `application/json` | always |
| `Content-Type` | `application/json` (`multipart/form-data` for uploads) | requests with a body |
| `Authorization` | `Bearer <token>` | every endpoint except `POST /auth/login` |
| `Accept-Language` | `en` (later `ur`) | always; server picks messages in that language |
| `User-Agent` | `PaksoftPOS/<version> (<android\|ios> <os version>)` | always |
| `X-Device-Id` | stable random UUID per install | always (helps support and token naming) |

The `.htaccess` of each Laravel app already forwards `Authorization` to PHP.

### 1.3 Authentication

- Laravel **Sanctum 2.x** personal access tokens (`personal_access_tokens` table). One token per device; `device_name` names it.
- Tokens do not expire by default. Logout deletes the current token. Deactivating a user (`is_active = 0`) or deleting them revokes all their tokens.
- Any `401` → the app clears the session and returns to login with "Your session ended. Please log in again."

### 1.4 JSON shape

- Keys are `snake_case`. Unknown keys must be ignored by the client (new fields are added without a version bump).
- Every documented key is **always present**; missing values are `null`.
- **Money** is a **string** with exactly two decimals: `"7998.00"`. Never a JSON number. The app parses it into `Money` (Long paisa).
- **Quantities and rates** are JSON numbers: `"qty": 2.5`, `"rate": 17`.
- **Dates** are `"YYYY-MM-DD"`. **Timestamps** are ISO-8601 with the shop's offset: `"2026-11-08T14:10:00+05:00"`.
- **IDs** are integers. **Enums** are lowercase strings (mapping in `DOMAIN.md` §3).
- **Images** are absolute URLs or `null`.

### 1.5 Envelope

Single resource:

```json
{ "data": { "id": 42, "name": "Pedestal Fan 14" } }
```

List:

```json
{
  "data": [ { "id": 42 } ],
  "meta": { "page": 1, "per_page": 30, "total": 1, "last_page": 1, "server_time": "2026-11-09T17:30:00+05:00" }
}
```

Error:

```json
{
  "error": {
    "code": "validation_failed",
    "message": "Please check the highlighted fields.",
    "fields": { "phone_number": ["This phone number is already used by another customer."] }
  }
}
```

`message` is human-readable and safe to show. `fields` is present (possibly empty `{}`) only for `validation_failed`.

### 1.6 Error codes

| HTTP | `code` | Meaning / app behavior |
|---|---|---|
| 400 | `bad_request` | Malformed JSON. Bug: log it. |
| 401 | `unauthenticated` | Missing/invalid token → logout. |
| 401 | `invalid_credentials` | Login only. Show message. |
| 403 | `account_inactive` | Login only. |
| 403 | `role_not_supported` | Login with role Customer (5). |
| 403 | `forbidden` | Missing permission. Show message; hide the action next time. |
| 404 | `not_found` | Row missing or outside the user's scope. |
| 409 | `conflict` | Stale write (ledger `base_updated_at` mismatch). Offer reload. |
| 422 | `validation_failed` | Show `fields` next to inputs. |
| 422 | `insufficient_stock` | Sale only; `details.lines` lists offending line indexes. |
| 422 | `totals_mismatch` | Sale only; server recomputed totals differ by > 0.01. `details` has server figures. Bug: log it, refresh catalog, let the cashier retry. |
| 429 | `too_many_requests` | Back off (`Retry-After` header). |
| 500 | `server_error` | Generic message; retry allowed for GET only. |
| 503 | `maintenance` | Laravel maintenance mode. |

`details` (object) may be added to any error for machine-readable context.

### 1.7 Lists, paging and sync

- `?page=` (1-based), `?per_page=` (default 30, max 100).
- `?search=` does a case-insensitive contains match on the documented fields.
- `?updated_since=<timestamp>` (catalog and customers only) returns rows with `updated_at >= value`, **including inactive ones** (`is_active: false`), so the cache can drop them. Use the previous response's `meta.server_time` as the next value. Hard deletes can't be detected this way, so the app also does a **full refresh** of these lists once a day and on pull-to-refresh.

### 1.8 Idempotency

Creating endpoints that a flaky network could repeat take a client-generated `client_uuid` (UUID v4):
`POST /sales`, `POST /sales/{id}/payments`, `POST /customers`.

The server stores it in a unique `client_uuid` column. A repeat with the same value returns **200** with the already-created resource and header `Idempotent-Replay: true` instead of creating a duplicate. The app keeps the same `client_uuid` across retries of the same user action.

### 1.9 Permissions and scope

Each endpoint lists its permission. The server checks it with the same `hasPermissionTo()` as the web, and applies the **staff `own` scope** (`DOMAIN.md` §2) to every list and detail. Rows outside the scope return `404`, not `403`.

### 1.10 Versioning

Additive changes (new fields, new endpoints, new enum values the app treats as `unknown`) stay in `v1`. Renames, removals and meaning changes require `v2`. The app maps unknown enum values to an `Unknown` case instead of crashing.

---

## Part 2 — Endpoints (Phase 0 and Phase 1)

Index:

| # | Method & path | Permission | Phase |
|---|---|---|---|
| A1 | `POST /auth/login` | — | 0 |
| A2 | `POST /auth/logout` | auth | 0 |
| A3 | `GET /me` | auth | 0 |
| A4 | `GET /bootstrap` | auth | 0 |
| D1 | `GET /dashboard` | auth | 1 |
| P1 | `GET /sellable-items` | `sales-add` | 1 |
| P2 | `GET /sellable-items/lookup` | `sales-add` | 1 |
| P3 | `GET /products` | `products-index` | 1 |
| P4 | `GET /products/{id}` | `products-index` | 1 |
| P5 | `GET /products/{id}/batches` | `sales-add` | 1 |
| C1 | `GET /customers` | `customers-index` or `sales-add` | 1 |
| C2 | `GET /customers/{id}` | `customers-index` or `sales-add` | 1 |
| C3 | `POST /customers` | `customers-add` | 1 |
| S1 | `GET /sales` | `sales-index` | 1 |
| S2 | `GET /sales/{id}` | `sales-index` | 1 |
| S3 | `POST /sales` | `sales-add` | 1 |
| S4 | `GET /sales/{id}/receipt` | `sales-index` | 1 |
| S5 | `GET /sales/{id}/payments` | `sales-index` | 1 |
| S6 | `POST /sales/{id}/payments` | `sales-add` | 1 |
| S7 | `DELETE /sales/{id}` | `sales-delete` | 1 (drafts only) |
| G1 | `GET /gift-cards` | `sales-add` | 1 |
| R1 | `GET /cash-registers/current` | `sales-add` | 1 |
| R2 | `POST /cash-registers` | `sales-add` | 1 |
| R3 | `GET /cash-registers/{id}` | `sales-add` | 1 |
| R4 | `POST /cash-registers/{id}/close` | `sales-add` | 1 |
| L1 | `GET /ledgers` | `sales-index` | 1 |
| L2 | `POST /ledgers` | `sales-add` | 1 |
| L3 | `GET /ledgers/{id}` | `sales-index` | 1 |
| L4 | `PUT /ledgers/{id}/grid` | `sales-edit` | 1 |
| L5 | `PATCH /ledgers/{id}` | `sales-edit` | 1 |
| L6 | `DELETE /ledgers/{id}` | `sales-delete` | 1 |
| L7 | `POST /ledgers/import` | `sales-add` | 1 |

---

### A1 `POST /auth/login`

Throttled to 10 per minute per IP + login.

Request:

```json
{ "login": "cashier1", "password": "••••••••", "device_name": "Pixel 8 (PBSF 1.0.0)" }
```

`login` is a username **or** an email (`DOMAIN.md` §2).

`200`:

```json
{
  "data": {
    "token": "12|Xn3r…",
    "user": {
      "id": 7,
      "name": "cashier1",
      "email": "cashier1@example.com",
      "phone": "0300 0000000",
      "company_name": null,
      "role": { "id": 1, "name": "Admin" },
      "warehouse_id": null,
      "biller_id": null
    },
    "permissions": ["sales-index", "sales-add", "products-index"],
    "staff_scope": "all"
  }
}
```

`staff_scope` is `"own"` when `general_settings.staff_access == "own"` and `role.id > 2`, else `"all"`.

Errors: `401 invalid_credentials`, `403 account_inactive`, `403 role_not_supported`, `422 validation_failed`.

### A2 `POST /auth/logout`

No body. `204`. Deletes the current token only.

### A3 `GET /me`

`200`: same `data` as A1 **without** `token`. Called on app start to refresh permissions.

### A4 `GET /bootstrap`

Everything the app needs after login, in one call. Cached in Room/DataStore; refetched on app start and pull-to-refresh.

`200`:

```json
{
  "data": {
    "shop": {
      "title": "Super madina Fan",
      "logo_url": "https://pbsf.org.pk/smf/public/logo/logo.png",
      "developed_by": "PAKSOFT Solutions",
      "date_format": "d/m/Y",
      "invoice_format": "standard",
      "gst_state": null,
      "timezone": "Asia/Karachi",
      "currency": { "id": 3, "code": "PKR", "name": "Pakistani Rupee", "exchange_rate": 1, "position": "prefix" },
      "features": { "ledger": true, "previous_balance": true }
    },
    "pos": {
      "default_customer_id": 1,
      "default_warehouse_id": 1,
      "default_biller_id": 1,
      "page_size": 20,
      "keyboard_active": false
    },
    "warehouses":      [ { "id": 1, "name": "Super Madina Fan", "phone": null, "email": null, "address": "Lahore", "is_active": true } ],
    "billers":         [ { "id": 1, "name": "Admin", "company_name": "BSF", "phone_number": "0300…", "email": "…", "address": "…", "city": "Lahore", "image_url": null, "is_active": true } ],
    "customer_groups": [ { "id": 1, "name": "General", "percentage": 0, "is_active": true } ],
    "categories":      [ { "id": 1, "name": "Product", "parent_id": null, "image_url": null, "is_active": true } ],
    "brands":          [ { "id": 1, "title": "SMF", "image_url": null, "is_active": true } ],
    "units":           [ { "id": 1, "code": "pc", "name": "Piece", "base_unit_id": null, "operator": "*", "operation_value": 1, "is_active": true } ],
    "taxes":           [ { "id": 1, "name": "GST 17%", "rate": 17, "is_active": true } ],
    "coupons":         [ { "id": 2, "code": "EID10", "type": "percentage", "amount": "10.00", "minimum_amount": null, "remaining": 40, "expired_date": "2026-12-31" } ],
    "accounts":        [ { "id": 1, "account_no": "11111", "name": "Sales Account", "is_default": true } ]
  }
}
```

Never include Stripe keys, mail/SMS settings or anything secret. `features.ledger` is `false` on shops without the Ledger tables; `features.previous_balance` reflects `Schema::hasColumn('sales','previous_balance')`.

---

### D1 `GET /dashboard`

Query: `period=today|last_7_days|this_month|this_year` (default `this_month`) **or** `start_date` + `end_date`. Optional `warehouse_id`.

`200`:

```json
{
  "data": {
    "period": { "start_date": "2026-11-01", "end_date": "2026-11-30" },
    "revenue": "15996.00",
    "sale_return": "0.00",
    "purchase_return": "0.00",
    "profit": "6094.00",
    "cash_flow": [
      { "month": "2026-06", "received": "0.00", "sent": "0.00" },
      { "month": "2026-11", "received": "15996.00", "sent": "120000.00" }
    ],
    "month_breakdown": { "month": "2026-11", "purchase": "120000.00", "revenue": "15996.00", "expense": "0.00" }
  }
}
```

Figures follow `HomeController` exactly (`DOMAIN.md` §9), including the staff `own` scope. `cash_flow` covers the same months as the web chart, oldest first.

---

### P1 `GET /sellable-items`

The POS catalog: products expanded into sellable items (`DOMAIN.md` §4), priced for one warehouse.

Query: `warehouse_id` (required), `search`, `category_id`, `brand_id`, `featured=true` (POS grid), `page`, `per_page`, `updated_since`.

`200` (list):

```json
{
  "data": [
    {
      "key": "p42",
      "product_id": 42,
      "variant_id": null,
      "code": "10000001",
      "name": "Pedestal Fan 14",
      "type": "standard",
      "image_url": null,
      "category_id": 1,
      "brand_id": 1,
      "featured": true,
      "is_batch": false,
      "base_price": "7998.00",
      "is_promotion": false,
      "tax": { "id": null, "name": "No Tax", "rate": 0, "method": "exclusive" },
      "units": [
        { "id": 1, "name": "Piece", "operator": "*", "operation_value": 1 }
      ],
      "stock": 15,
      "alert_quantity": 5,
      "is_active": true,
      "updated_at": "2026-11-08T12:01:00+05:00"
    }
  ],
  "meta": { "page": 1, "per_page": 30, "total": 1, "last_page": 1, "server_time": "2026-11-09T17:30:00+05:00" }
}
```

- `key` is unique per item: `p<product_id>` or `p<product_id>v<variant_id>`.
- `base_price` already applies `DOMAIN.md` §4 steps 1–4 (promotion, variant extra, warehouse price, currency). The app applies steps 5–7 (customer group, sale unit, manual override).
- `units`: first entry is the product's default **sale unit**. Empty array for non-standard items.
- `stock`: for the warehouse, in **base units**; `null` for combo/digital/service.
- Includes only items sellable in that warehouse (§4 "Stock availability").

### P2 `GET /sellable-items/lookup`

Barcode scan or typed code. Query: `code` (required), `warehouse_id` (required).
`200`: one item (same shape as P1). `404 not_found` when no active product or variant has that code.

### P3 `GET /products`

Product list screen. Query: `search` (name, code), `category_id`, `brand_id`, `page`, `per_page`.

```json
{
  "data": [
    {
      "id": 42, "name": "Pedestal Fan 14", "code": "10000001", "type": "standard",
      "image_url": null, "brand": "SMF", "category": "Product", "unit": "Piece",
      "qty": 15, "price": "7998.00", "cost": "6000.00",
      "stock_worth_price": "119970.00", "stock_worth_cost": "90000.00",
      "is_active": true
    }
  ],
  "meta": { "page": 1, "per_page": 30, "total": 1, "last_page": 1, "server_time": "2026-11-09T17:30:00+05:00" }
}
```

`cost` and `stock_worth_cost` are `null` unless the user has `products-edit` (matches the web's column visibility for staff).

### P4 `GET /products/{id}`

```json
{
  "data": {
    "id": 42, "name": "Pedestal Fan 14", "code": "10000001", "type": "standard",
    "barcode_symbology": "C128", "image_urls": [],
    "brand": { "id": 1, "title": "SMF" }, "category": { "id": 1, "name": "Product" },
    "unit": { "id": 1, "name": "Piece" }, "sale_unit_id": 1, "purchase_unit_id": 1,
    "price": "7998.00", "cost": "6000.00", "qty": 15, "alert_quantity": 5,
    "promotion": { "active": false, "price": null, "starts": null, "ends": null },
    "tax": { "id": null, "name": "No Tax", "rate": 0, "method": "exclusive" },
    "is_variant": false, "is_batch": false, "featured": true,
    "details_html": null,
    "stock_by_warehouse": [ { "warehouse_id": 1, "warehouse": "Super Madina Fan", "qty": 15, "price": null } ],
    "variants": [],
    "combo_items": [],
    "is_active": true
  }
}
```

`variants[]`: `{ "variant_id", "name", "item_code", "additional_price", "qty" }`. `combo_items[]`: `{ "product_id", "name", "qty", "price" }`.

### P5 `GET /products/{id}/batches`

Query: `warehouse_id` (required). `200`: `[ { "id": 3, "batch_no": "B-2026-11", "expired_date": "2027-06-30", "qty": 40 } ]`, only batches with stock in that warehouse.

---

### C1 `GET /customers`

Query: `search` (name, phone, company), `customer_group_id`, `page`, `per_page`, `updated_since`.

```json
{
  "data": [
    {
      "id": 1, "name": "Walk in Customer", "company_name": null,
      "phone_number": "0000", "email": null, "city": "Lahore",
      "customer_group_id": 1, "deposit_balance": "0.00",
      "is_active": true, "updated_at": "2026-11-01T09:00:00+05:00"
    }
  ],
  "meta": { "page": 1, "per_page": 30, "total": 1, "last_page": 1, "server_time": "2026-11-09T17:30:00+05:00" }
}
```

`deposit_balance` = `deposit − expense`.

### C2 `GET /customers/{id}`

C1 item plus `address`, `state`, `postal_code`, `country`, `tax_no`, `customer_group` (`{id, name, percentage}`), `total_due` (sum of unpaid grand totals) and `recent_sales` (last 10, S1 item shape).

### C3 `POST /customers`

Quick add from the POS (fields of the POS "Add Customer" modal).

```json
{
  "client_uuid": "6f1c1f0e-8f6a-4f0e-9d7c-0b9a7e5d2c11",
  "customer_group_id": 1,
  "name": "Example Traders",
  "phone_number": "0301 2345678",
  "email": null,
  "address": "",
  "city": ""
}
```

Validation: `name` and `customer_group_id` required; `phone_number` required and unique among **active** customers (`CustomerController::store`); `address` and `city` may be empty (the DB columns are NOT NULL, so store `""`). `201` with the C2 shape. Errors: `422 validation_failed`.

---

### S1 `GET /sales`

Query: `date_from`, `date_to` (default: last 30 days), `warehouse_id`, `status` (`completed|pending|draft`), `payment_status` (`pending|due|partial|paid`), `search` (reference no, customer name), `page`, `per_page`.

```json
{
  "data": [
    {
      "id": 4, "reference_no": "posr-20261108-020720",
      "created_at": "2026-11-08T14:07:20+05:00",
      "customer": { "id": 1, "name": "Walk in Customer" },
      "biller": { "id": 1, "name": "Admin" },
      "warehouse": { "id": 1, "name": "Super Madina Fan" },
      "status": "completed", "payment_status": "paid",
      "grand_total": "7998.00", "paid_amount": "7998.00", "due": "0.00"
    }
  ],
  "meta": { "page": 1, "per_page": 30, "total": 4, "last_page": 1, "server_time": "2026-11-09T17:30:00+05:00",
            "totals": { "grand_total": "15996.00", "paid_amount": "15996.00", "due": "0.00" } }
}
```

`meta.totals` sums the whole filtered set, not just the page (the web shows these under the table).

### S2 `GET /sales/{id}`

S1 item plus:

```json
{
  "item": 1, "total_qty": 1,
  "total_discount": "0.00", "total_tax": "0.00", "total_price": "7998.00",
  "order_tax_rate": 0, "order_tax": "0.00", "order_discount": "0.00",
  "coupon": null, "coupon_discount": "0.00", "shipping_cost": "0.00",
  "previous_balance": "0.00",
  "sale_note": null, "staff_note": null,
  "user": { "id": 7, "name": "cashier1" },
  "lines": [
    {
      "product_id": 42, "variant_id": null, "product_batch_id": null,
      "name": "Pedestal Fan 14", "code": "10000001", "batch_no": null,
      "qty": 1, "unit": { "id": 1, "name": "Piece" },
      "net_unit_price": "7998.00", "discount": "0.00",
      "tax_rate": 0, "tax": "0.00", "subtotal": "7998.00"
    }
  ],
  "payments": [ "…S5 item…" ]
}
```

### S3 `POST /sales`

Creates a POS sale or a draft, or finalizes a draft. The app computes the figures with `CartCalculator` (`DOMAIN.md` §5) and sends them; the **server recomputes** every line and total from its own data and the submitted inputs, and rejects differences over `0.01` with `422 totals_mismatch`.

```json
{
  "client_uuid": "0b5e2c4a-3f7d-4c55-8a2e-5d3a1b9c7e10",
  "status": "completed",
  "draft_sale_id": null,
  "reference_no": null,
  "customer_id": 1,
  "warehouse_id": 1,
  "biller_id": 1,
  "lines": [
    {
      "product_id": 42,
      "custom_name": null,
      "variant_id": null,
      "product_batch_id": null,
      "code": "10000001",
      "qty": 1,
      "sale_unit_id": 1,
      "unit_price": "7998.00",
      "discount_per_unit": "0.00",
      "tax_rate": 0,
      "net_unit_price": "7998.00",
      "tax": "0.00",
      "subtotal": "7998.00"
    }
  ],
  "item": 1,
  "total_qty": 1,
  "total_discount": "0.00",
  "total_tax": "0.00",
  "total_price": "7998.00",
  "order_tax_rate": 0,
  "order_tax": "0.00",
  "order_discount": "0.00",
  "coupon_id": null,
  "coupon_discount": "0.00",
  "shipping_cost": "0.00",
  "grand_total": "7998.00",
  "previous_balance": "0.00",
  "payment": {
    "method": "cash",
    "tendered": "8000.00",
    "amount": "7998.00",
    "cheque_no": null,
    "gift_card_id": null,
    "note": null
  },
  "sale_note": null,
  "staff_note": null
}
```

Rules:

- `status`: `completed` or `draft`. Drafts: `payment` must be `null`; no stock change.
- `draft_sale_id`: finalizing a draft deletes it first (legacy behavior).
- **Custom item:** `product_id: null` + `custom_name: "Fitting charges"` (+ `unit_price`). Server uses `customProductId()` (`DOMAIN.md` §6.6).
- `unit_price` = price after customer group + sale unit + manual override (§4 steps 5–7). The server **trusts `unit_price`** (the web lets any POS user override prices too) but verifies: the product/variant/batch exists and is sellable in the warehouse, `tax_rate` equals the product's tax rate, `sale_unit_id` belongs to the product, the arithmetic of every line (`DOMAIN.md` §5), every order total, the coupon, and stock.
- Staff (role > 2): `warehouse_id` and `biller_id` must equal the user's own (`DOMAIN.md` §4 "POS defaults"), else `403 forbidden`.
- `payment.method`: `cash | cheque | gift_card | deposit`. `card` and `paypal` return `422 validation_failed` (web-only for now).
- `payment.amount` (> 0, ≤ `grand_total` + `previous_balance`) is what's recorded; `tendered − amount` is the change. `payment: null` with status `completed` → sale is **Due**.
- Gift card: `gift_card_id` required; balance ≥ amount. Deposit: customer `deposit_balance` ≥ amount. Cheque: `cheque_no` required.
- If the user has an open cash register for `warehouse_id`, the sale and payment are attached to it.
- `previous_balance` is ignored (stored as 0) when the shop lacks the column.

`201`:

```json
{ "data": { "sale": "…S2 shape…", "receipt": "…S4 shape…" } }
```

Errors: `422 validation_failed`, `insufficient_stock`, `totals_mismatch`; `403 forbidden`. Replay with the same `client_uuid` → `200` + `Idempotent-Replay: true`.

### S4 `GET /sales/{id}/receipt`

Everything needed to draw the receipt natively (`DOMAIN.md` §7), already formatted by the server so the app and web print the same numbers.

```json
{
  "data": {
    "shop": { "title": "Super madina Fan", "logo_url": "https://…", "developed_by": "PAKSOFT Solutions" },
    "biller": { "name": "Admin", "company_name": "BSF", "address": "…", "city": "Lahore", "phone_number": "…", "email": "…" },
    "reference_no": "posr-20261108-020720",
    "date": "2026-11-08T14:07:20+05:00",
    "customer": { "name": "Walk in Customer", "phone_number": "0000", "address": "" },
    "lines": [ { "name": "Pedestal Fan 14", "code": "10000001", "qty": 1, "unit": "Piece", "unit_price": "7998.00", "subtotal": "7998.00" } ],
    "totals": [
      { "label": "Subtotal", "amount": "7998.00", "emphasis": false },
      { "label": "Grand Total (PKR)", "amount": "7998.00", "emphasis": true },
      { "label": "Paid", "amount": "7998.00", "emphasis": false }
    ],
    "payments": [ { "method": "cash", "amount": "7998.00", "change": "2.00" } ],
    "amount_in_words": "seven thousand nine hundred ninety-eight",
    "sale_note": null,
    "barcode_value": "posr-20261108-020720"
  }
}
```

`totals` is the ordered, already-filtered row list from `DOMAIN.md` §7 (rows with zero values are left out by the server), so the app just draws it.

### S5 `GET /sales/{id}/payments`

```json
{
  "data": [
    {
      "id": 9, "payment_reference": "spr-20261108-020720",
      "created_at": "2026-11-08T14:07:20+05:00",
      "method": "cash", "amount": "7998.00", "change": "2.00",
      "cheque_no": null, "gift_card_no": null, "note": null,
      "user": { "id": 7, "name": "cashier1" }, "account": { "id": 1, "name": "Sales Account" }
    }
  ]
}
```

### S6 `POST /sales/{id}/payments`

Collect a due amount later (`SaleController::addPayment`).

```json
{ "client_uuid": "…", "method": "cash", "tendered": "500.00", "amount": "500.00", "cheque_no": null, "gift_card_id": null, "note": "Installment" }
```

`201` with the updated S2 sale (new `paid_amount`, `payment_status`). `amount` must be ≤ the sale's due.

### S7 `DELETE /sales/{id}`

Phase 1 allows deleting **drafts only** (`status == draft`); other statuses return `422 validation_failed`. `204`.

---

### G1 `GET /gift-cards`

Query: `customer_id` (optional). Active, unexpired cards with a positive balance:
`[ { "id": 3, "card_no": "4782 1100", "balance": "1500.00", "expired_date": "2027-01-31", "customer_id": 12 } ]`.

---

### R1 `GET /cash-registers/current`

Query: `warehouse_id` (required). `200`: `{ "data": null }` when none is open, else the R3 shape.

### R2 `POST /cash-registers`

`{ "warehouse_id": 1, "cash_in_hand": "5000.00" }` → `201` R3 shape. `422 validation_failed` if one is already open for this user + warehouse.

### R3 `GET /cash-registers/{id}`

```json
{
  "data": {
    "id": 5, "warehouse_id": 1, "status": "open", "opened_at": "2026-11-09T09:00:00+05:00",
    "cash_in_hand": "5000.00",
    "total_sale_amount": "15996.00", "total_payment": "15996.00",
    "cash_payment": "15996.00", "cheque_payment": "0.00", "gift_card_payment": "0.00",
    "credit_card_payment": "0.00", "deposit_payment": "0.00", "paypal_payment": "0.00",
    "total_sale_return": "0.00", "total_expense": "0.00", "total_cash": "20996.00"
  }
}
```

Figures follow `CashRegisterController::getDetails`.

### R4 `POST /cash-registers/{id}/close`

No body. `200` with the R3 shape and `"status": "closed"`.

---

### L1 `GET /ledgers`

```json
{
  "data": {
    "folders": [
      { "key": "sales", "title": "Sales Ledgers",
        "files": [ { "id": 1, "name": "Example Traders", "rows_count": 1, "cols_count": 7, "updated_at": "2026-11-08T14:10:00+05:00" } ] },
      { "key": "purchase", "title": "Purchase Ledgers", "files": [] }
    ]
  }
}
```

Files sorted by name. `403 forbidden` when the shop has no Ledger (`features.ledger == false`).

### L2 `POST /ledgers`

`{ "folder": "sales", "name": "Example Traders" }` → `201` with the L3 shape (default header + 15 blank rows, `DOMAIN.md` §10).

### L3 `GET /ledgers/{id}`

```json
{
  "data": {
    "id": 1, "folder": "sales", "name": "Example Traders",
    "grid": [
      ["Date", "Customer / Details", "Qty", "Rate", "Amount", "Received", "Balance"],
      ["1", "", "", "", "", "", ""]
    ],
    "rows_count": 1, "cols_count": 7,
    "updated_at": "2026-11-08T14:10:00+05:00",
    "limits": { "max_rows": 5000, "max_cols": 40, "max_cell_chars": 1000 }
  }
}
```

### L4 `PUT /ledgers/{id}/grid`

Autosave (debounced ~1.5 s after the last edit, plus on leaving the screen).

```json
{ "grid": [["Date", "…"], ["1", "…"]], "base_updated_at": "2026-11-08T14:10:00+05:00" }
```

- Server runs `LedgerSheet::cleanGrid` and `storeGrid`.
- If `base_updated_at` ≠ the row's current `updated_at`, return `409 conflict` with `details.current` = the L3 shape, so the app can offer "Reload" or "Keep mine" (Keep mine resends with the new `base_updated_at`). This is stricter than the web (last write wins), on purpose, because a phone and the web may edit the same file.

`200`: `{ "data": { "saved_at": "2026-11-09T17:31:02+05:00", "updated_at": "2026-11-09T17:31:02+05:00", "rows_count": 2, "cols_count": 7 } }`

### L5 `PATCH /ledgers/{id}`

`{ "name": "Example Traders 2026" }` → `200` L3 shape without `grid`.

### L6 `DELETE /ledgers/{id}`

`204`.

### L7 `POST /ledgers/import`

`multipart/form-data`: `folder` (`sales|purchase`), `sheet_file` (`.csv` or `.xlsx`). `201`: `{ "data": { "created": [ { "id": 2, "name": "Imported Ledger" } ] } }`. Errors as in `LedgerSheetController::import`, mapped to `422 validation_failed` with the same messages.

---

## Part 3 — Later phases (outline only)

Write the full contract for each before building it, following Part 1. Paths:

| Area | Endpoints |
|---|---|
| Purchases | `GET/POST /purchases`, `GET /purchases/{id}`, `GET/POST /purchases/{id}/payments` |
| Returns | `GET/POST /sale-returns`, `GET/POST /purchase-returns` |
| Expenses | `GET /expense-categories`, `GET/POST /expenses` |
| Quotations | `GET/POST /quotations`, `POST /quotations/{id}/convert-to-sale` |
| Transfers, adjustments, stock counts | `GET/POST /transfers`, `GET/POST /adjustments`, `GET/POST /stock-counts` |
| Customers extra | `GET/POST /customers/{id}/deposits` |
| Suppliers, billers | `GET/POST /suppliers`, `GET/POST /billers` |
| Reports | `GET /reports/{name}` with the same filters as `ReportController` |
| Accounting | `GET /accounts`, `GET /accounts/balance-sheet`, `GET /accounts/{id}/statement`, `GET/POST /money-transfers` |
| HRM | `departments`, `employees`, `attendance`, `payroll`, `holidays` |
| Admin | `users`, `roles`, settings (read-only first) |

---

## Part 4 — Server implementation guide (Laravel 6, PHP 7.4)

Work in the **staging copy first**, then roll out to `smf` → `bsf` → `mummyandme`.

### 4.1 Staging

1. Copy `public_html/pbsf.org.pk/smf` to `smf-staging` on the server and clone the database (`mysqldump` → new DB). Point `smf-staging/.env` at the copy and set `APP_DEBUG=false`.
2. Block `dbBackup/`, `storage/`, `vendor/`, `database/`, `.env` from the web in `smf-staging/.htaccess` (and do the same on the live apps; see `README.md` → Security).

### 4.2 Sanctum

On a machine with **PHP 7.4** and Composer (the server runs PHP 7.4 per the root `.htaccess`):

```bash
composer require laravel/sanctum:^2.15
php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"
php artisan migrate   # creates personal_access_tokens
```

Add `Laravel\Sanctum\HasApiTokens` to `App\User`. Upload the changed `composer.json`, `composer.lock`, `config/sanctum.php`, the migration and the updated `vendor/`. Protect routes with `auth:sanctum`.

### 4.3 Files to add

```
routes/api.php                                  Route::prefix('v1')->group(...)
app/Http/Controllers/Api/V1/AuthController.php
app/Http/Controllers/Api/V1/BootstrapController.php
app/Http/Controllers/Api/V1/DashboardController.php
app/Http/Controllers/Api/V1/SellableItemController.php
app/Http/Controllers/Api/V1/ProductController.php
app/Http/Controllers/Api/V1/CustomerController.php
app/Http/Controllers/Api/V1/SaleController.php
app/Http/Controllers/Api/V1/SalePaymentController.php
app/Http/Controllers/Api/V1/GiftCardController.php
app/Http/Controllers/Api/V1/CashRegisterController.php
app/Http/Controllers/Api/V1/LedgerController.php
app/Http/Resources/Api/V1/*Resource.php         one per response shape above; Money via a helper
app/Http/Requests/Api/V1/*Request.php           validation; failures rendered as 422 validation_failed
app/Http/Middleware/Api/EnsureActiveUser.php    401/403 JSON instead of the web redirect
app/Http/Middleware/Api/RequirePermission.php   'perm:sales-add' → 403 forbidden JSON
app/Services/SaleService.php                    shared by web + API (see 4.4)
app/Services/PricingService.php                 DOMAIN §4–5 on the server
app/Services/DashboardService.php
app/Support/Api/ApiError.php                    builds the error envelope
database/migrations/2026_11_xx_*.php            see 4.5
tests/Feature/Api/V1/*Test.php
```

`app/Exceptions/Handler.php`: when `$request->is('api/*')`, render every exception as the JSON envelope (`AuthenticationException` → 401 `unauthenticated`, `ValidationException` → 422, `ModelNotFoundException` → 404, `ThrottleRequestsException` → 429, everything else → 500 without stack traces).

Raise the `api` throttle in `app/Http/Kernel.php` from `60,1` to `120,1`; login uses its own `throttle:10,1`.

### 4.4 Share logic, don't copy it

`SaleController::store` mixes request parsing, stock changes, payments and redirects. Extract the body into `App\Services\SaleService::create(array $input, User $user): Sale` in small steps:

1. Write a feature test that posts the web POS form on staging data and records the resulting rows (`sales`, `product_sales`, `payments`, stock in `products`, `product_warehouse`, `product_variants`, `product_batches`).
2. Move the code into the service unchanged; the web controller calls the service and keeps its redirects. The test must still pass.
3. The API controller validates the JSON body (above), recomputes and compares totals with `PricingService`, then maps it into the **same input array the web form posts** and calls the same service inside a DB transaction. Mapping (legacy names from `pos.blade.php`):

| API (S3) | Legacy form field | Conversion |
|---|---|---|
| `status` | `sale_status` | `completed` → `1`, `draft` → `3` |
| — | `pos` | always `1` |
| `draft_sale_id` | `draft`, `sale_id` | `draft = 1` and `sale_id = id` when set, else `draft = 0` |
| `customer_id`, `warehouse_id`, `biller_id`, `reference_no` | same names | as is (`reference_no` omitted when null) |
| `lines[].product_id` / `custom_name` | `product_id[]` | id, or `"custom:" . urlencode(custom_name)` |
| `lines[].code` | `product_code[]` | as is |
| `lines[].product_batch_id` | `product_batch_id[]` | id or empty |
| `lines[].qty` | `qty[]` | as is |
| `lines[].sale_unit_id` | `sale_unit[]` | **unit name** (`units.unit_name`), or `n/a` for non-standard items |
| `lines[].net_unit_price` | `net_unit_price[]` | as is |
| `lines[].discount_per_unit` | `discount[]` | **× qty** (the web posts the line's total discount) |
| `lines[].tax_rate`, `tax`, `subtotal` | `tax_rate[]`, `tax[]`, `subtotal[]` | as is |
| `item`, `total_qty`, `total_discount`, `total_tax`, `total_price`, `order_tax_rate`, `order_tax`, `order_discount`, `shipping_cost`, `grand_total`, `previous_balance`, `sale_note`, `staff_note` | same names | as is |
| `coupon_id`, `coupon_discount` | `coupon_active`, `coupon_id`, `coupon_discount` | `coupon_active = 1` when `coupon_id` is set |
| `payment.method` | `paid_by_id` | `cash` 1, `gift_card` 2, `cheque` 4, `deposit` 6 |
| `payment.tendered` | `paying_amount` | as is |
| `payment.amount` | `paid_amount` | as is; `0` when `payment` is null |
| `payment.cheque_no`, `gift_card_id`, `note` | `cheque_no`, `gift_card_id`, `payment_note` | as is |
| — | `payment_status` | computed by the service for POS sales (`DOMAIN.md` §3) |

Do the same for payments (`addPayment`), the dashboard (`HomeController`) and cash register figures. Never duplicate the stock rules in the API controllers.

### 4.5 Migrations

The live databases were changed by hand. New migrations must be **guarded** so they run safely everywhere:

| Migration | Guard |
|---|---|
| `create_ledger_sheets_table` | `if (!Schema::hasTable('ledger_sheets'))` |
| `add_previous_balance_to_sales_table` (`double`, nullable) | `if (!Schema::hasColumn('sales','previous_balance'))` |
| `add_client_uuid_to_sales_payments_customers` (`char(36)`, nullable, unique) | per table `hasColumn` |
| Sanctum `create_personal_access_tokens_table` | from the package |

Take a database backup before running migrations on a live shop, and store it **outside** `public_html`.

### 4.6 Tests

PHPUnit feature tests per endpoint against a MySQL test database seeded from a staging dump (the legacy code uses MySQL-specific queries, so SQLite won't do). Cover: auth errors, permission `403`, staff `own` scope, every validation rule, `insufficient_stock`, `totals_mismatch`, idempotent replay, and the ledger `409`.
