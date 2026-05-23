package features.auth

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import shared.UserSessionPrincipal

fun Route.logout(database: Database) {
    authenticate {
        post("/logout") {
            val session = call.principal<UserSessionPrincipal>()!!
            database.logoutQueries.deleteSession(session.sessionId.toString())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}