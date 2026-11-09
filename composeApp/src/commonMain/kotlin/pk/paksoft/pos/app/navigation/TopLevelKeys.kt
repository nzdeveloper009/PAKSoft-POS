package pk.paksoft.pos.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Placeholder tabs until their feature modules exist; More's key lives in feature:settings.

@Serializable
data object DashboardKey : NavKey

@Serializable
data object PosKey : NavKey

@Serializable
data object SalesKey : NavKey

@Serializable
data object LedgerKey : NavKey
