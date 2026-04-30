package dev.jakobdario.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserEntity(
    val id: Int,
    val username: String,
    val email: String,
    val apartmentId: Int?,
)