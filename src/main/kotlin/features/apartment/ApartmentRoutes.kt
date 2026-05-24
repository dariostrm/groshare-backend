package dev.jakobdario.features.apartment

import dev.jakobdario.database.Database
import io.ktor.server.routing.Route

fun Route.apartmentRoutes(database: Database) {
    getApartment(database)
    createApartment(database)
}