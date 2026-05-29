package dev.jakobdario.features.invites

import dev.jakobdario.database.Database
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.validation.ValidationException

fun Route.rejectInvite(database: Database) {
    val db = database.rejectInviteQueries

    authenticate {
        delete("/invites/{inviteId}") {
            val inviteId = call.parameters["inviteId"]?.toLongOrNull()
                ?: throw ValidationException("The invite ID must be provided as a number")

            db.deleteInvite(inviteId)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}