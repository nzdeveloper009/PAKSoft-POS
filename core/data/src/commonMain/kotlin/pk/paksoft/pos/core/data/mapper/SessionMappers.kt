package pk.paksoft.pos.core.data.mapper

import pk.paksoft.pos.core.model.AppUser
import pk.paksoft.pos.core.model.StaffScope
import pk.paksoft.pos.core.model.UserSession
import pk.paksoft.pos.core.network.dto.SessionDto

internal fun SessionDto.toUserSession(): UserSession = UserSession(
    user = AppUser(
        id = user.id,
        name = user.name,
        email = user.email,
        phone = user.phone,
        companyName = user.companyName,
        roleId = user.role.id,
        roleName = user.role.name,
        warehouseId = user.warehouseId,
        billerId = user.billerId,
    ),
    permissions = permissions.toSet(),
    staffScope = when (staffScope) {
        "all" -> StaffScope.All
        "own" -> StaffScope.Own
        else -> StaffScope.Unknown
    },
)
