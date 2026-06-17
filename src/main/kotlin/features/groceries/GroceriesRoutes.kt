package dev.jakobdario.features.groceries

import dev.jakobdario.database.Database
import features.apartment.createApartment
import features.apartment.editApartment
import features.apartment.getApartment
import io.ktor.server.routing.Route

fun Route.groceriesRoutes(database: Database) {
    getGroceries(database)
}