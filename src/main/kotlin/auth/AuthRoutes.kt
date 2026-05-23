package auth

import UserSessionPrincipal
import dev.jakobdario.database.Database
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import io.ktor.server.routing.Route
import java.util.UUID
import kotlin.time.Clock

fun Application.configureAuth(database: Database) {
    install(Authentication) {
        bearer {
            realm = "GroShare"
            authenticate { tokenCredential ->
                val sessionId = try {
                    UUID.fromString(tokenCredential.token)
                } catch (e: IllegalArgumentException) {
                    return@authenticate null
                }

                val currentTime = Clock.System.now().epochSeconds
                val userId = database.authQueries.getUserId(sessionId.toString(), currentTime).executeAsOneOrNull()
                if (userId != null)
                    UserSessionPrincipal(userId, sessionId)
                else null
            }
        }
    }
}

fun Route.authRoutes(database: Database) {
    login(database)
    signup(database)
    logout(database)
}