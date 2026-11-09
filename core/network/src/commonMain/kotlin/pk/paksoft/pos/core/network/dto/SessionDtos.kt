package pk.paksoft.pos.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("login") val login: String,
    @SerialName("password") val password: String,
    @SerialName("device_name") val deviceName: String,
)

@Serializable
data class SessionDto(
    @SerialName("token") val token: String? = null,
    @SerialName("user") val user: UserDto,
    @SerialName("permissions") val permissions: List<String>,
    @SerialName("staff_scope") val staffScope: String,
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String?,
    @SerialName("company_name") val companyName: String?,
    @SerialName("role") val role: RoleDto,
    @SerialName("warehouse_id") val warehouseId: Long?,
    @SerialName("biller_id") val billerId: Long?,
)

@Serializable
data class RoleDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
)
