package shared

import java.util.UUID

data class UserSessionPrincipal(
    val userId: Long,
    val sessionId: UUID
)
