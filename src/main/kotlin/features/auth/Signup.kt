package features.auth

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import shared.ErrorResponse
import shared.SecurityConfig.SESSION_EXPIRATION_SECONDS
import shared.validation.ensureValidEmail
import shared.validation.ensureValidPassword
import shared.validation.ensureValidUsername
import java.sql.SQLException
import java.util.UUID
import kotlin.time.Clock


@Serializable
data class SignUpRequest(val username: String, val email: String, val password: String) {
    fun sanitizeAndEnsureValid(): SignUpRequest {
        val copy = copy(
            username = username.trim(),
            email = email.trim(),
            password = password.trim()
        )
        copy.username.ensureValidUsername()
        copy.email.ensureValidEmail()
        copy.password.ensureValidPassword()
        return copy
    }
}
@Serializable
data class SignUpResponse(val token: String)

fun Route.signup(database: Database) {
    val db = database.signupQueries

    post("/signup") {
        val request = call.receive<SignUpRequest>()
        val (username, email, password) = request.sanitizeAndEnsureValid()

        val hashedPassword = PasswordManager.hashPassword(password)
        val sessionId = UUID.randomUUID()
        val expiresAt = Clock.System.now().epochSeconds + SESSION_EXPIRATION_SECONDS

        try {
            db.transaction {
                val userId = db.insertUser(
                    email = email,
                    username = username,
                    password = hashedPassword.value,
                ).executeAsOne()

                db.createSession(
                    id = sessionId.toString(),
                    userId = userId,
                    expiresAt = expiresAt,
                )
            }

            call.respond(HttpStatusCode.Created, SignUpResponse(sessionId.toString()))

        } catch (e: SQLException) {
            // SQLite error code 19 is SQLITE_CONSTRAINT
            if (e.errorCode == 19)
                call.respond(HttpStatusCode.Conflict,
                    ErrorResponse("Username or email already exists"))
            else
                throw e
        }
    }
}