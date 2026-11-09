package pk.paksoft.pos.app.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource
import pk.paksoft.pos.app.navigation.DashboardKey
import pk.paksoft.pos.app.navigation.LedgerKey
import pk.paksoft.pos.app.navigation.PosKey
import pk.paksoft.pos.app.navigation.SalesKey
import pk.paksoft.pos.app.resources.Res
import pk.paksoft.pos.app.resources.nav_dashboard
import pk.paksoft.pos.app.resources.nav_ledger
import pk.paksoft.pos.app.resources.nav_more
import pk.paksoft.pos.app.resources.nav_pos
import pk.paksoft.pos.app.resources.nav_sales
import pk.paksoft.pos.core.model.Permissions
import pk.paksoft.pos.feature.settings.navigation.MoreKey

/**
 * The bottom bar / rail tabs, in the web sidebar's order. A tab shows only when the user has every
 * permission in [requires] and, for [needsLedger], the shop has the ledger feature.
 */
enum class TopLevelDestination(
    val key: NavKey,
    val label: StringResource,
    val icon: ImageVector,
    val requires: Set<String>,
    val needsLedger: Boolean,
) {
    DASHBOARD(DashboardKey, Res.string.nav_dashboard, Icons.Filled.Home, emptySet(), needsLedger = false),
    POS(PosKey, Res.string.nav_pos, Icons.Filled.ShoppingCart, setOf(Permissions.SALES_ADD), needsLedger = false),
    SALES(SalesKey, Res.string.nav_sales, Icons.AutoMirrored.Filled.List, setOf(Permissions.SALES_INDEX), needsLedger = false),
    LEDGER(LedgerKey, Res.string.nav_ledger, Icons.Filled.Edit, setOf(Permissions.SALES_INDEX), needsLedger = true),
    MORE(MoreKey, Res.string.nav_more, Icons.Filled.Menu, emptySet(), needsLedger = false),
}

fun visibleDestinations(permissions: Set<String>, ledgerEnabled: Boolean): List<TopLevelDestination> =
    TopLevelDestination.entries.filter { destination ->
        permissions.containsAll(destination.requires) && (ledgerEnabled || !destination.needsLedger)
    }
