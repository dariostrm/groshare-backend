package dev.jakobdario.features.groceries

import dev.jakobdario.database.Database
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import shared.NotFoundException
import shared.UnauthorizedException
import shared.UserSessionPrincipal
import shared.validation.ensureValidGroceryName

@Serializable
data class AddGroceryRequest(
    val name: String,
) {
    fun sanitizeAndEnsureValid(): AddGroceryRequest {
        val sanitizedName = name.trim()
        sanitizedName.ensureValidGroceryName()
        return copy(name = sanitizedName)
    }
}

@Serializable
data class AddGroceryResponse(val id: Long)

fun Route.addGrocery(database: Database) {
    val db = database.addGroceryQueries

    authenticate {
        post("/apartment/groceries") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<AddGroceryRequest>()
            val (groceryName) = request.sanitizeAndEnsureValid()

            val id = db.transactionWithResult {
                val result = db.getApartmentIdByUserId(session.userId).executeAsOneOrNull()
                    ?: throw UnauthorizedException("Account no longer exists")
                val apartmentId = result.apartment_id
                    ?: throw NotFoundException("Cannot add a grocery because you are not in any apartment")

                db.insertGrocery(
                    apartmentId = apartmentId,
                    name = groceryName,
                    creatorId = session.userId,
                ).executeAsOne()
            }
            call.respond(HttpStatusCode.Created, AddGroceryResponse(id))
        }
    }
}