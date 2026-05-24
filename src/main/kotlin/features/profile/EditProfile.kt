package features.profile

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable
import shared.ErrorResponse
import shared.UserSessionPrincipal
import shared.validation.ensureValidEmail
import shared.validation.ensureValidUsername
import java.sql.SQLException

@Serializable
data class EditProfileRequest(val username: String, val email: String) {
    fun ensureValid() {
        username.ensureValidUsername()
        email.ensureValidEmail()
    }
}

fun Route.editProfile(database: Database) {
    val db = database.editProfileQueries

    authenticate {
        put("/profile") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<EditProfileRequest>()
            request.ensureValid()

            try {
                db.updateProfile(
                    userId = session.userId,
                    username = request.username,
                    email = request.email,
                )

                call.respond(HttpStatusCode.NoContent)

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
}