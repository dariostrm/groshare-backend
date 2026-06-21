package dev.jakobdario.features.payments

import dev.jakobdario.database.Database
import io.ktor.server.routing.Route

fun Route.paymentRoutes(database: Database) {
    buyGroceries(database)
    getDebts(database)
}