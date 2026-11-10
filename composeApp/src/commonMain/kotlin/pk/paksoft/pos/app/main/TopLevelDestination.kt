package pk.paksoft.pos.app.main

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
import pk.paksoft.pos.core.designsystem.icon.PosIcons
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
    DASHBOARD(DashboardKey, Res.string.nav_dashboard, PosIcons.Home, emptySet(), needsLedger = false),
    POS(PosKey, Res.string.nav_pos, PosIcons.Pos, setOf(Permissions.SALES_ADD), needsLedger = false),
    SALES(SalesKey, Res.string.nav_sales, PosIcons.Sales, setOf(Permissions.SALES_INDEX), needsLedger = false),
    LEDGER(LedgerKey, Res.string.nav_ledger, PosIcons.Ledger, setOf(Permissions.SALES_INDEX), needsLedger = true),
    MORE(MoreKey, Res.string.nav_more, PosIcons.More, emptySet(), needsLedger = false),
}

fun visibleDestinations(permissions: Set<String>, ledgerEnabled: Boolean): List<TopLevelDestination> =
    TopLevelDestination.entries.filter { destination ->
        permissions.containsAll(destination.requires) && (ledgerEnabled || !destination.needsLedger)
    }
