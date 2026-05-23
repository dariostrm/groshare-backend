package features.profile

import dev.jakobdario.database.Database
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import shared.UserSessionPrincipal

fun Route.leaveApartment(database: Database) {
    authenticate {
        delete("/profile/apartment") {
            val session = call.principal<UserSessionPrincipal>()!!

            database.leaveApartmentQueries.leaveApartment(session.userId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}