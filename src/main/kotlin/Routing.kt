package dev.jakobdario

import dev.jakobdario.auth.HashServiceImpl
import dev.jakobdario.auth.authRoutes
import dev.jakobdario.repositories.SessionRepository
import dev.jakobdario.repositories.UserRepository
import dev.jakobdario.repositories.UserRepositorySqlite
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.OpenApiDocSource

fun Application.configureRouting(
    sessionRepository: SessionRepository,
) {
    val hashService = HashServiceImpl()
    val userRepository : UserRepository = UserRepositorySqlite()
    routing {
        route("/api/v1") {
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {
                
            }
            get("/") {
                call.respondText("Hello World!")
            }
            get {
                val users = userRepository.getUsers()
                call.respond(users)
            }
            authRoutes(userRepository, sessionRepository, hashService)
        }
    }
}
