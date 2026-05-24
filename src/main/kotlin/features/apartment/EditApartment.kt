package features.apartment

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable
import shared.UserSessionPrincipal
import shared.validation.ensureValidAddress
import shared.validation.ensureValidApartmentName
import shared.validation.ensureValidCity

@Serializable
data class EditApartmentRequest(
    val name: String,
    val address: String,
    val city: String,
) {
    fun sanitizeAndEnsureValid(): EditApartmentRequest {
        val copy = copy(
            name = name.trim(),
            address = address.trim(),
            city = city.trim(),
        )
        copy.name.ensureValidApartmentName()
        copy.address.ensureValidAddress()
        copy.city.ensureValidCity()
        return copy
    }
}

fun Route.editApartment(database: Database) {
    val db = database.editApartmentQueries

    authenticate {
        put("/apartment") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<EditApartmentRequest>()
            val (name, address, city) = request.sanitizeAndEnsureValid()

            db.updateApartment(
                name = name,
                address = address,
                city = city,
                userId = session.userId
            )

            call.respond(HttpStatusCode.NoContent)
        }
    }
}