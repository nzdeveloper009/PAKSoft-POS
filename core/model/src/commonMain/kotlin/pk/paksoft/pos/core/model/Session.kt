package pk.paksoft.pos.core.model

data class AppUser(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val companyName: String?,
    val roleId: Int,
    val roleName: String,
    val warehouseId: Long?,
    val billerId: Long?,
)

enum class StaffScope { All, Own, Unknown }

data class UserSession(
    val user: AppUser,
    val permissions: Set<String>,
    val staffScope: StaffScope,
) {
    fun can(permission: String): Boolean = permission in permissions
}

sealed interface SessionState {
    data object Starting : SessionState
    data object NoShop : SessionState
    data class LoggedOut(val shop: Shop, val notice: SessionNotice?) : SessionState
    data class LoggedIn(val shop: Shop, val session: UserSession) : SessionState
}

enum class SessionNotice { SessionExpired }
