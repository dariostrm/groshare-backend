package features.profile

import dev.jakobdario.database.Database
import io.ktor.server.routing.Route

fun Route.profileRoutes(database: Database) {
    getProfile(database)
    editProfile(database)
    leaveApartment(database)
}