package pk.paksoft.pos.core.network

/** JSON examples copied verbatim from docs/API.md (A1, A3, A4) plus the error envelopes of Part 1. */
object ApiFixtures {
    val LOGIN_200 = """{
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
}""".trimIndent()

    /** A3 is "the same data as A1 without token". */
    val ME_200 = """{
  "data": {
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
}""".trimIndent()

    val BOOTSTRAP_200 = """{
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
}""".trimIndent()

    val ERROR_INVALID_CREDENTIALS = """{"error":{"code":"invalid_credentials","message":"The username/email or password is incorrect."}}"""

    val ERROR_VALIDATION_DEVICE_NAME = """{"error":{"code":"validation_failed","message":"Please check the highlighted fields.","fields":{"device_name":["The device name field is required."]}}}"""

    val ERROR_UNAUTHENTICATED = """{"error":{"code":"unauthenticated","message":"Your session ended. Please log in again."}}"""

    val ERROR_SERVER = """{"error":{"code":"server_error","message":"Something went wrong on the server. Please try again."}}"""
}
