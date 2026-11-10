package pk.paksoft.pos.core.network.demo

/** Invented data for the debug-only Demo shop. Shaped like docs/API.md A1/A3/A4; nothing here comes from a real shop. */
internal object DemoFixtures {
    private const val SESSION_BODY = """
    "user": {
      "id": 1,
      "name": "Ayesha (demo)",
      "email": "demo@example.invalid",
      "phone": null,
      "company_name": null,
      "role": { "id": 1, "name": "Admin" },
      "warehouse_id": 1,
      "biller_id": 1
    },
    "permissions": ["sales-index", "sales-add"],
    "staff_scope": "all""""

    val LOGIN: String = """{"data":{"token":"demo-token",$SESSION_BODY}}"""

    val ME: String = """{"data":{$SESSION_BODY}}"""

    val BOOTSTRAP: String = """{
  "data": {
    "shop": {
      "title": "Demo shop",
      "logo_url": null,
      "developed_by": "PAKSOFT Solutions",
      "date_format": "d/m/Y",
      "invoice_format": "standard",
      "gst_state": null,
      "timezone": "Asia/Karachi",
      "currency": { "id": 1, "code": "PKR", "name": "Pakistani Rupee", "exchange_rate": 1, "position": "prefix" },
      "features": { "ledger": true, "previous_balance": true }
    },
    "pos": {
      "default_customer_id": null,
      "default_warehouse_id": 1,
      "default_biller_id": 1,
      "page_size": 20,
      "keyboard_active": false
    },
    "warehouses": [ { "id": 1, "name": "Main store", "phone": null, "email": null, "address": null, "is_active": true } ],
    "billers": [ { "id": 1, "name": "Demo biller", "company_name": "Demo Solar Traders", "phone_number": null, "email": null, "address": null, "city": null, "image_url": null, "is_active": true } ],
    "customer_groups": [],
    "categories": [],
    "brands": [],
    "units": [],
    "taxes": [],
    "coupons": [],
    "accounts": []
  }
}"""

    const val NOT_FOUND: String = """{"error":{"code":"not_found","message":"Not found."}}"""
}
