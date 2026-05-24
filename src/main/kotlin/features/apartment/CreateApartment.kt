package dev.jakobdario.features.apartment

import dev.jakobdario.database.Database
import dev.jakobdario.shared.ConflictException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import shared.UserSessionPrincipal
import shared.validation.ensure

@Serializable
data class CreateApartmentRequest(
    val name: String,
    val address: String,
    val city: String,
) {
    fun ensureValid() {
        ensure(name.isNotBlank()) { "Name must not be blank" }
        ensure(name.length in 2..50) { "Name must be between 2 and 50 characters long" }
        ensure(address.isNotBlank()) { "Address must not be blank" }
        ensure(address.length in 5..100) { "Address must be between 5 and 100 characters long" }
        ensure(city.isNotBlank()) { "City must not be blank" }
        ensure(city.length in 2..50) { "City must be between 2 and 50 characters long" }
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
            request.ensureValid()

            val apartmentId = db.transactionWithResult {
                db.getApartmentId(session.userId).executeAsOneOrNull()?.let {
                    throw ConflictException("User is already part of an apartment")
                }

                val apartmentId = db.createApartment(
                    name = request.name,
                    address = request.address,
                    city = request.city,
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