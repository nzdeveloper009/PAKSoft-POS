package pk.paksoft.pos.core.ui

/**
 * The address a shop is shown with under its name: its API base URL without the scheme and the `/api/v1` suffix
 * (`https://pbsf.org.pk/smf/api/v1` → `pbsf.org.pk/smf`, `http://10.0.2.2:8000/api/v1` → `10.0.2.2:8000`).
 */
fun shopHost(apiBaseUrl: String): String =
    apiBaseUrl
        .substringAfter("://")
        .trimEnd('/')
        .removeSuffix(API_SUFFIX)
        .trimEnd('/')

private const val API_SUFFIX = "/api/v1"
