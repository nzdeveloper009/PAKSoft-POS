package pk.paksoft.pos.core.common

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationScopeTest {
    @Test
    fun `a failing child is handled and the scope keeps running`() = runTest {
        val d = StandardTestDispatcher(testScheduler)
        val scope = ApplicationScope(AppDispatchers(d, d, d))

        scope.launch { error("boom") }
        advanceUntilIdle()
        var ranAfterFailure = false
        scope.launch { ranAfterFailure = true }
        advanceUntilIdle()

        assertTrue(scope.isActive)
        assertTrue(ranAfterFailure)
        scope.cancel()
    }
}
