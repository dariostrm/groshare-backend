package dev.jakobdario.entities

import kotlinx.serialization.Serializable

@Serializable
data class InviteEntity(
    val id: Int,
    val inviterId: Int,
    val invitedUserId: Int,
    val apartmentId: Int,
    val status: InviteStatus
)

enum class InviteStatus {
    Pending,
    Accepted,
    Rejected,
}
