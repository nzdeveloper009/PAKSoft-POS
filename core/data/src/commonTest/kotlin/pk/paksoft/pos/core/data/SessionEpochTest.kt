package pk.paksoft.pos.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SessionEpochTest {
    private val epoch = SessionEpoch()

    @Test
    fun `write in the epoch it started in runs`() = runTest {
        val startedAt = epoch.current()
        var writes = 0

        assertTrue(epoch.writeIfCurrent(startedAt) { writes++ })

        assertEquals(1, writes)
    }

    @Test
    fun `write started before a clear is dropped`() = runTest {
        val startedAt = epoch.current()
        var clears = 0
        var writes = 0

        epoch.clear { clears++ }

        assertFalse(epoch.writeIfCurrent(startedAt) { writes++ })
        assertEquals(1, clears)
        assertEquals(0, writes)
    }

    @Test
    fun `a failing clear still ends the epoch`() = runTest {
        val startedAt = epoch.current()

        runCatching { epoch.clear { error("disk full") } }

        assertFalse(epoch.writeIfCurrent(startedAt) {})
    }
}
