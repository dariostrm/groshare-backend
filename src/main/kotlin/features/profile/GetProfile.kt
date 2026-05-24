package features.profile

import dev.jakobdario.database.Database
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import dev.jakobdario.shared.exceptions.UnauthorizedException
import shared.UserSessionPrincipal

@Serializable
data class GetProfileResponse(
    val username: String,
    val email: String,
    val apartmentName: String?
)

fun Route.getProfile(database: Database) {
    authenticate {
        get("/profile") {
            val session = call.principal<UserSessionPrincipal>()!!

            val profile = database.getProfileQueries.getProfile(session.userId).executeAsOneOrNull()
                ?: throw UnauthorizedException("Account no longer exists")

            call.respond(GetProfileResponse(
                username = profile.username,
                email = profile.email,
                apartmentName = profile.name
            ))
        }
    }
}