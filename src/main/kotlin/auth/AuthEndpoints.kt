package dev.jakobdario.auth

import dev.jakobdario.getSession
import dev.jakobdario.repositories.SessionRepository
import dev.jakobdario.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)
@Serializable
data class SignUpRequest(val username: String, val email: String, val password: String)
@Serializable
data class AuthResponse(val token: String)

fun Route.authRoutes(
    userRepository: UserRepository,
    sessionRepository: SessionRepository,
    hashService: HashService
) {
    post("/login") {
        val request = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            return@post call.respond(HttpStatusCode.BadRequest, "Invalid request body")
        }

        val user = userRepository.getUserByUsername(request.username)
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        val valid = userRepository.checkPassword(user.id) { hashed ->
            hashService.verify(request.password, hashed)
        }
        if (!valid) return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        val sessionId = sessionRepository.createSession(user.id)
        call.respond(HttpStatusCode.OK, AuthResponse(sessionId.toString()))
    }
    post("/signup") {
        val request = try {
            call.receive<SignUpRequest>()
        } catch (_: Exception) {
            return@post call.respond(HttpStatusCode.BadRequest, "Invalid request body")
        }

        if (!userRepository.checkUniqueUsername(request.username)) {
            return@post call.respond(HttpStatusCode.Conflict, "Username is already taken")
        }
        if (!userRepository.checkUniqueEmail(request.email)) {
            return@post call.respond(HttpStatusCode.Conflict, "Email is already taken")
        }

        try {
            val user = userRepository.signUp(request.username, request.email, hashService.hashPassword(request.password))
            val sessionId = sessionRepository.createSession(user.id)
            call.respond(HttpStatusCode.OK, AuthResponse(sessionId.toString()))
        } catch (e: Exception) {
            return@post call.respond(HttpStatusCode.InternalServerError, "An error occurred while creating the user")
        }
    }
    authenticate() {
        post("/logout") {
            val session = getSession()
            sessionRepository.deleteSession(session.sessionId)
            call.respond(HttpStatusCode.OK, "Logged out successfully")
        }
    }
}