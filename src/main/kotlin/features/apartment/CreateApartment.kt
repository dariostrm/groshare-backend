package features.apartment

import dev.jakobdario.database.Database
import shared.ConflictException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import shared.UserSessionPrincipal
import shared.validation.ensureValidAddress
import shared.validation.ensureValidApartmentName
import shared.validation.ensureValidCity

@Serializable
data class CreateApartmentRequest(
    val name: String,
    val address: String,
    val city: String,
) {
    fun sanitizeAndEnsureValid(): CreateApartmentRequest {
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

@Serializable
data class CreateApartmentResponse(val apartmentId: Long)

fun Route.createApartment(database: Database) {
    val db = database.createApartmentQueries

    authenticate {
        post("/apartment") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<CreateApartmentRequest>()
            val (name, address, city) = request.sanitizeAndEnsureValid()

            val apartmentId = db.transactionWithResult {
                val currentApartmentId = db.getApartmentId(session.userId).executeAsOneOrNull()
                if (currentApartmentId != null)
                    throw ConflictException("User is already part of an apartment")

                val apartmentId = db.createApartment(
                    name = name,
                    address = address,
                    city = city,
                ).executeAsOne()

                db.joinApartment(
                    apartmentId = apartmentId,
                    userId = session.userId,
                )
                apartmentId
            }

            call.respond(HttpStatusCode.Created, CreateApartmentResponse(apartmentId))
        }
    }
}