package features.invites

import dev.jakobdario.database.Database
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import shared.*
import shared.validation.ValidationException
import shared.validation.ensureValidUsername
import java.sql.SQLException
import kotlin.time.Clock

@Serializable
data class SendInviteRequest(
    val username: String,
) {
    fun sanitizeAndEnsureValid(): SendInviteRequest {
        val sanitizedUsername = username.trim()
        sanitizedUsername.ensureValidUsername()
        return copy(username = sanitizedUsername)
    }
}

fun Route.sendInvite(database: Database) {
    val db = database.sendInviteQueries

    authenticate {
        post("/apartment/invites") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<SendInviteRequest>()
            val (username) = request.sanitizeAndEnsureValid()

            try {
                db.transaction {
                    val result = db.getApartmentIdByUserId(session.userId).executeAsOneOrNull()
                        ?: throw UnauthorizedException("Account no longer exists")
                    val apartmentId = result.apartment_id
                        ?: throw ForbiddenException("Cannot send an invite because you are not in any apartment")

                    val recipientId = db.getUserIdByUsername(username).executeAsOneOrNull()
                        ?: throw NotFoundException("User with username '$username' not found")

                    if (recipientId == session.userId)
                        throw ValidationException("You cannot invite yourself to your apartment")

                    db.insertInvite(
                        apartmentId = apartmentId,
                        inviterId = session.userId,
                        invitedUserId = recipientId,
                        createdAt = Clock.System.now().epochSeconds,
                    )
                }
                call.respond(HttpStatusCode.NoContent)

            } catch (e: SQLException) {
                // SQLite error code 19 is SQLITE_CONSTRAINT
                if (e.errorCode == 19)
                    call.respond(HttpStatusCode.Conflict,
                        ErrorResponse("This user has already been invited to this apartment"))
                else
                    throw e
            }
        }
    }
}