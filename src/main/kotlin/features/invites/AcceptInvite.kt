package features.invites

import dev.jakobdario.database.Database
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import shared.ConflictException
import shared.ForbiddenException
import shared.NotFoundException
import shared.UserSessionPrincipal
import shared.validation.ValidationException
import java.sql.SQLException

fun Route.acceptInvite(database: Database) {
    val db = database.acceptInviteQueries

    authenticate {
        post("/invites/{inviteId}/accept") {
            val inviteId = call.parameters["inviteId"]?.toLongOrNull()
                ?: throw ValidationException("The invite ID must be provided as a number")
            val session = call.principal<UserSessionPrincipal>()!!

            try {
                db.transaction {

                    val currentApartment = db.getApartmentId(session.userId).executeAsOneOrNull()
                    if (currentApartment != null)
                        throw ConflictException("Please leave your apartment before accepting an invite to another one")

                    val (apartmentId, invitedUserId) = db.getInvite(inviteId).executeAsOneOrNull()
                        ?: throw NotFoundException("Invite $inviteId not found")

                    if (invitedUserId != session.userId)
                        throw ForbiddenException("You were not the invited user of this invite")

                    db.deleteInvite(inviteId)

                    db.addUserToApartment(apartmentId, session.userId)
                }

                call.respond(HttpStatusCode.NoContent)

            } catch (e: SQLException) {
                // SQLite error code 19 is SQLITE_CONSTRAINT
                // (if the foreign key constraint fails
                // that means we tried adding a user to an apartment that does not exist)
                if (e.errorCode == 19)
                    throw NotFoundException("The apartment you were invited to no longer exists")
                else
                    throw e
            }
        }
    }
}