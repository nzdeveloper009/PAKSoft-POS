package pk.paksoft.pos.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DataEnvelope<T>(@SerialName("data") val data: T)

@Serializable
data class ErrorEnvelopeDto(@SerialName("error") val error: ErrorBodyDto)

@Serializable
data class ErrorBodyDto(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
    @SerialName("fields") val fields: Map<String, List<String>>? = null,
    /** Documented as an object, but PHP encodes an empty one as `[]`, so any JSON value is accepted. */
    @SerialName("details") val details: JsonElement? = null,
)
