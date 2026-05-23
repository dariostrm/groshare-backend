package dev.jakobdario

import features.auth.authRoutes
import dev.jakobdario.database.Database
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import features.profile.profileRoutes

fun Application.configureRouting(database: Database) {

    routing {
        route("/api/v1") {
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {

            }
            get {
                call.respondText("Hello World!")
            }
            authRoutes(database)
            profileRoutes(database)
        }
    }
}
