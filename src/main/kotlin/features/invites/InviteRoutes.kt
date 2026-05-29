package features.invites

import dev.jakobdario.database.Database
import dev.jakobdario.features.invites.acceptInvite
import dev.jakobdario.features.invites.getInvites
import dev.jakobdario.features.invites.rejectInvite
import io.ktor.server.routing.Route

fun Route.inviteRoutes(database: Database) {
    sendInvite(database)
    getInvites(database)
    acceptInvite(database)
    rejectInvite(database)
}