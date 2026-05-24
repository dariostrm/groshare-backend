package dev.jakobdario.features.apartment

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import shared.ErrorResponse
import shared.UserSessionPrincipal

@Serializable
data class GetApartmentResponse(
    val apartmentId: Long,
    val name: String,
    val address: String,
    val city: String,
    val roommates: List<Roommate>,
)

@Serializable
data class Roommate(
    val id: Long,
    val username: String
)

fun Route.getApartment(database: Database) {
    val db = database.getApartmentQueries

    authenticate {
        get("/apartment") {
            val session = call.principal<UserSessionPrincipal>()!!

            val apartment = db.getApartmentByUserId(session.userId).executeAsOneOrNull()
                ?: return@get call.respond(HttpStatusCode.NotFound)

            val roommates = db.getRoomatesByApartmentId(apartment.id).executeAsList()
                .map { Roommate(id = it.id, username = it.username) }

            call.respond(HttpStatusCode.OK, GetApartmentResponse(
                apartmentId = apartment.id,
                name = apartment.name,
                address = apartment.address,
                city = apartment.city,
                roommates = roommates
            ))
        }
    }
}