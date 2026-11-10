package pk.paksoft.pos.core.model

data class Shop(
    val id: String,
    val displayName: String,
    val apiBaseUrl: String,
    val isDevelopment: Boolean,
)

object Shops {
    const val DEMO_ID = "demo"

    /** Reserved, non-resolvable host: the debug network engine answers it in-app (never leaves the device). */
    const val DEMO_HOST = "demo.paksoft.invalid"

    val demo: Shop = Shop(DEMO_ID, "Demo shop", "https://$DEMO_HOST/api/v1", isDevelopment = true)

    val production: List<Shop> = listOf(
        Shop("smf", "Super Madina Solar Fan", "https://pbsf.org.pk/smf/api/v1", isDevelopment = false),
        Shop("bsf", "BSF", "https://pbsf.org.pk/bsf/api/v1", isDevelopment = false),
        Shop("mummyandme", "Mummy & Me", "https://pos.mummyandme.com.pk/api/v1", isDevelopment = false),
    )

    /** Production shops, plus Demo, Staging and Local (dev) in debug builds. */
    fun forBuild(isDebug: Boolean, localApiBaseUrl: String): List<Shop> =
        if (!isDebug) production
        else production + listOf(
            demo,
            Shop("staging", "Staging (dev)", "https://pbsf.org.pk/smf-staging/api/v1", isDevelopment = true),
            Shop("local", "Local API (dev)", localApiBaseUrl, isDevelopment = true),
        )
}
