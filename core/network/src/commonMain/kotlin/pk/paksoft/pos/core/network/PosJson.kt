package pk.paksoft.pos.core.network

import kotlinx.serialization.json.Json

/** The one JSON configuration for the API (docs/API.md 1.4: unknown keys are ignored). */
val PosJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
