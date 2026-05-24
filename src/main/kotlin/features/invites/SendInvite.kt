package features.invites

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import shared.ErrorResponse
import shared.ForbiddenException
import shared.NotFoundException
import shared.UnauthorizedException
import shared.UserSessionPrincipal
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
                        throw BadRequestException("You cannot invite yourself to your apartment")

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