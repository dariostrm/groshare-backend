package dev.jakobdario.entities

import kotlinx.serialization.Serializable

@Serializable
data class ApartmentEntity(
    val id: Int,
    val name: String,
    val address: String,
    val city: String
)
