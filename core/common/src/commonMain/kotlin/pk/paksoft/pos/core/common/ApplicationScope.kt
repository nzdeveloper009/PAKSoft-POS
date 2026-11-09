package pk.paksoft.pos.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import org.koin.core.annotation.Singleton

/** App-lifetime scope for work that must outlive a screen; a failing child does not cancel its siblings. */
@Singleton
class ApplicationScope(dispatchers: AppDispatchers) : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.default
}
