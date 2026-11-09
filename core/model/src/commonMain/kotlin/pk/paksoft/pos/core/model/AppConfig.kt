package pk.paksoft.pos.core.model

data class AppConfig(
    val isDebug: Boolean,
    val platformName: String,
    val osVersion: String,
    val appVersion: String,
    val localApiBaseUrl: String,
)
