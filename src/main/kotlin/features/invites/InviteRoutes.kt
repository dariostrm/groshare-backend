package features.invites

import dev.jakobdario.database.Database
import io.ktor.server.routing.Route

fun Route.inviteRoutes(database: Database) {
    sendInvite(database)
}