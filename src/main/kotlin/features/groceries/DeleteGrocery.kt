package dev.jakobdario.features.groceries

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import shared.ForbiddenException
import shared.NotFoundException
import shared.UnauthorizedException
import shared.UserSessionPrincipal
import shared.validation.ValidationException

fun Route.deleteGrocery(database: Database) {
    val db = database.deleteGroceryQueries

    authenticate {
        delete("/apartment/groceries/{groceryId}") {
            val groceryId = call.parameters["groceryId"]?.toLongOrNull()
                ?: throw ValidationException("The grocery ID must be provided as a number")
            val session = call.principal<UserSessionPrincipal>()!!

            db.transaction {
                val result = db.getApartmentIdByUserId(session.userId).executeAsOneOrNull()
                    ?: throw UnauthorizedException("Account no longer exists")
                val apartmentId = result.apartment_id
                    ?: throw NotFoundException("Cannot delete a grocery because you are not in any apartment")

                val grocerysApartmentId = db.getGrocerysApartmentByID(groceryId).executeAsOneOrNull()
                    ?: throw NotFoundException("Cannot find a grocery with the given ID")

                if (apartmentId != grocerysApartmentId)
                    throw ForbiddenException("You don't have the permission to delete this grocery")

                db.deleteGrocery(groceryId)
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}