package dev.jakobdario.features.apartment

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import shared.UserSessionPrincipal

@Serializable
data class GetApartmentResponse(
    val apartmentId: Long,
    val name: String,
    val address: String,
    val city: String,
    val roommatesCount: Long,
)

fun Route.getApartment(database: Database) {
    authenticate {
        get("/apartment") {
            val session = call.principal<UserSessionPrincipal>()!!

            val response = database.getApartmentQueries.getApartmentByUserId(
                userId = session.userId,
                mapper = { apartmentId, name, address, city, roommatesCount ->
                    GetApartmentResponse(
                        apartmentId = apartmentId,
                        name = name,
                        address = address,
                        city = city,
                        roommatesCount = roommatesCount
                    )
                }
            ).executeAsOneOrNull()

            if (response != null)
                call.respond(HttpStatusCode.OK, response)
            else
                call.respond(HttpStatusCode.NotFound)
        }
    }
}