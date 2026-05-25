package dev.jakobdario.features.invites

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import shared.UserSessionPrincipal
import shared.db.Invites

@Serializable
data class Invite(
    val inviteId: Long,
    val inviterName: String,
    val apartmentName: String,
    val sentAtInEpochSeconds: Long,
)

@Serializable
data class GetInvitesResponse(val invites: List<Invite>)

fun Route.getInvites(database: Database) {
    val db = database.getInvitesQueries

    authenticate {
        get("/invites") {
            val session = call.principal<UserSessionPrincipal>()!!

            val invites = db.getInvitesForUserId(session.userId)
                .executeAsList()
                .map {
                    Invite(
                        inviteId = it.id,
                        inviterName = it.username,
                        apartmentName = it.name,
                        sentAtInEpochSeconds = it.created_at
                    )
                }

            call.respond(HttpStatusCode.OK, GetInvitesResponse(invites))
        }
    }
}