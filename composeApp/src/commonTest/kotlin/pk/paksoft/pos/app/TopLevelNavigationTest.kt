package pk.paksoft.pos.app

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pk.paksoft.pos.app.main.TopLevelDestination
import pk.paksoft.pos.app.main.backStackFor
import pk.paksoft.pos.app.main.selectedDestination
import pk.paksoft.pos.app.navigation.DashboardKey
import pk.paksoft.pos.app.navigation.SalesKey
import pk.paksoft.pos.feature.settings.navigation.MoreKey

class TopLevelNavigationTest {
    /** Stands in for a future screen opened on top of a tab (e.g. a sale's detail). */
    private data object DetailKey : NavKey

    @Test fun `dashboard is the whole back stack`() {
        assertEquals(listOf<NavKey>(DashboardKey), backStackFor(TopLevelDestination.DASHBOARD))
    }

    @Test fun `every other tab sits on top of dashboard`() {
        assertEquals(listOf<NavKey>(DashboardKey, SalesKey), backStackFor(TopLevelDestination.SALES))
        assertEquals(listOf<NavKey>(DashboardKey, MoreKey), backStackFor(TopLevelDestination.MORE))
    }

    @Test fun `selected tab is the last top level key`() {
        assertEquals(TopLevelDestination.SALES, selectedDestination(listOf(DashboardKey, SalesKey)))
        assertEquals(TopLevelDestination.DASHBOARD, selectedDestination(listOf(DashboardKey)))
    }

    @Test fun `a screen opened on top of a tab keeps that tab selected`() {
        assertEquals(TopLevelDestination.SALES, selectedDestination(listOf(DashboardKey, SalesKey, DetailKey)))
    }

    @Test fun `empty stack has no selected tab`() {
        assertNull(selectedDestination(emptyList()))
    }
}
