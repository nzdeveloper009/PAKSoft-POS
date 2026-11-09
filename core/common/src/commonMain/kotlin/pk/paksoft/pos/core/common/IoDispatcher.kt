package pk.paksoft.pos.core.common

import kotlinx.coroutines.CoroutineDispatcher

/** `Dispatchers.IO` is not visible from common code, so each platform supplies it. */
internal expect fun ioDispatcher(): CoroutineDispatcher
