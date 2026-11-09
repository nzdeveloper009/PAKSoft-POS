package pk.paksoft.pos.core.common

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import org.koin.core.annotation.Singleton

/**
 * App-lifetime scope for work that must outlive a screen; a failing child does not cancel its siblings.
 * An uncaught failure is logged (exception type only: a message could carry user data) instead of crashing the app.
 */
@Singleton
class ApplicationScope(dispatchers: AppDispatchers) : CoroutineScope {
    private val handler = CoroutineExceptionHandler { _, e ->
        Logger.withTag("ApplicationScope").e { "Uncaught ${e::class.simpleName}" }
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.default + handler
}
