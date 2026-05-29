package features.invites

import dev.jakobdario.database.Database
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.ForbiddenException
import shared.NotFoundException
import shared.UserSessionPrincipal
import shared.validation.ValidationException

fun Route.rejectInvite(database: Database) {
    val db = database.rejectInviteQueries

    authenticate {
        delete("/invites/{inviteId}") {
            val session = call.principal<UserSessionPrincipal>()!!
            val inviteId = call.parameters["inviteId"]?.toLongOrNull()
                ?: throw ValidationException("The invite ID must be provided as a number")

            db.transaction {
                val invitedUserId = db.getInvitedUserId(inviteId).executeAsOneOrNull()
                    ?: throw NotFoundException("Invite $inviteId not found")

                if (invitedUserId != session.userId)
                    throw ForbiddenException("This invite does not belong to you")

                db.deleteInvite(inviteId)
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}