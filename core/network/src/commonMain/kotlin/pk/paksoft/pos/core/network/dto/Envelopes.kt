package pk.paksoft.pos.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DataEnvelope<T>(@SerialName("data") val data: T)

@Serializable
data class ErrorEnvelopeDto(@SerialName("error") val error: ErrorBodyDto)

@Serializable
data class ErrorBodyDto(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String,
    @SerialName("fields") val fields: Map<String, List<String>>? = null,
    @SerialName("details") val details: JsonObject? = null,
)
