package pk.paksoft.pos.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import pk.paksoft.pos.app.main.TopLevelDestination
import pk.paksoft.pos.app.main.visibleDestinations

class TopLevelDestinationTest {
    @Test fun `admin with ledger sees all five`() {
        assertEquals(
            TopLevelDestination.entries.toList(),
            visibleDestinations(setOf("sales-index", "sales-add"), ledgerEnabled = true),
        )
    }

    @Test fun `ledger hidden when the shop has no ledger`() {
        assertFalse(TopLevelDestination.LEDGER in visibleDestinations(setOf("sales-index", "sales-add"), false))
    }

    @Test fun `staff without sales permissions sees dashboard and more only`() {
        assertEquals(
            listOf(TopLevelDestination.DASHBOARD, TopLevelDestination.MORE),
            visibleDestinations(emptySet(), true),
        )
    }

    @Test fun `sales index without sales add hides pos only`() {
        assertEquals(
            listOf(
                TopLevelDestination.DASHBOARD,
                TopLevelDestination.SALES,
                TopLevelDestination.LEDGER,
                TopLevelDestination.MORE,
            ),
            visibleDestinations(setOf("sales-index"), ledgerEnabled = true),
        )
    }
}
