package features.invites

import dev.jakobdario.database.Database
import dev.jakobdario.features.invites.getInvites
import io.ktor.server.routing.Route

fun Route.inviteRoutes(database: Database) {
    sendInvite(database)
    getInvites(database)
}