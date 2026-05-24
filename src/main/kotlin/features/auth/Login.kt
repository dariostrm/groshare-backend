package features.auth

import dev.jakobdario.database.Database
import shared.UnauthorizedException
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import shared.SecurityConfig.SESSION_EXPIRATION_SECONDS
import shared.validation.ensureValidPassword
import shared.validation.ensureValidUsername
import java.util.UUID
import kotlin.time.Clock

@Serializable
data class LoginRequest(val username: String, val password: String) {
    fun sanitizeAndEnsureValid(): LoginRequest {
        val copy = copy(
            username = username.trim(),
            password = password,
        )
        copy.username.ensureValidUsername()
        copy.password.ensureValidPassword()
        return copy
    }
}

@Serializable
data class LoginResponse(val token: String)

fun Route.login(database: Database) {
    val db = database.loginQueries

    post("/login") {
        val request = call.receive<LoginRequest>()
        val (username, password) = request.sanitizeAndEnsureValid()

        val sessionId = UUID.randomUUID()
        database.transaction {
            val (passwordHash, userId) = db.getPasswordHashAndIdByUsername(username).executeAsOneOrNull()
                ?: throw UnauthorizedException()
            val validPw = PasswordManager.verify(password, PasswordHash(passwordHash))
            if (!validPw)
                throw UnauthorizedException()

            db.createSession(
                id = sessionId.toString(),
                userId = userId,
                expiresAt = Clock.System.now().epochSeconds + SESSION_EXPIRATION_SECONDS,
            )
        }
        call.respond(HttpStatusCode.OK, LoginResponse(sessionId.toString()))
    }
}