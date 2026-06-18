package dev.jakobdario.features.groceries

import dev.jakobdario.database.Database
import io.ktor.server.routing.*

fun Route.groceriesRoutes(database: Database) {
    getGroceries(database)
    addGrocery(database)
    deleteGrocery(database)
}