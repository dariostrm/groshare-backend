package dev.jakobdario

import dev.jakobdario.auth.HashServiceImpl
import dev.jakobdario.auth.authRoutes
import dev.jakobdario.profile.profileRoutes
import dev.jakobdario.repositories.SessionRepository
import dev.jakobdario.repositories.UserRepository
import dev.jakobdario.repositories.UserRepositorySqlite
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.principal
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun RoutingContext.getSession() : UserSessionPrincipal {
    return requireNotNull(call.principal<UserSessionPrincipal>()) {
        "Authenticated route executed without a UserSessionPrincipal"
    }
}

fun Application.configureRouting(
    sessionRepository: SessionRepository,
) {
    val hashService = HashServiceImpl()
    val userRepository : UserRepository = UserRepositorySqlite()
    routing {
        route("/api/v1") {
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {

            }
            get {
                call.respondText("Hello World!")
            }
            authRoutes(userRepository, sessionRepository, hashService)
            profileRoutes(userRepository)
        }
    }
}
