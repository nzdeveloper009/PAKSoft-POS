package pk.paksoft.pos.core.common

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** A random lowercase UUID string, used for `client_uuid`. */
@OptIn(ExperimentalUuidApi::class)
fun newUuid(): String = Uuid.random().toString()
