package dev.jakobdario.features.invites

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import shared.ConflictException
import shared.UserSessionPrincipal
import shared.validation.ValidationException

fun Route.acceptInvite(database: Database) {
    val db = database.acceptInviteQueries

    authenticate {
        post("/invites/{inviteId}/accept") {
            val inviteId = call.parameters["inviteId"]?.toLongOrNull()
                ?: throw ValidationException("The invite ID must be provided as a number")
            
            val session = call.principal<UserSessionPrincipal>()!!
            
            db.transaction {
                db.getApartmentId(session.userId).executeAsOneOrNull()
                    ?: throw ConflictException("Please leave your apartment before accepting an invite to another one")

                val invitedToApartmentId = db.deleteInvite(inviteId).executeAsOne()

                db.addUserToApartment(invitedToApartmentId, session.userId)
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}