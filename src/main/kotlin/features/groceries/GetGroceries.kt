package dev.jakobdario.features.groceries

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import shared.ForbiddenException
import shared.UnauthorizedException
import shared.UserSessionPrincipal

@Serializable
data class Grocery(
    val id: Long,
    val name: String,
    val addedByUsername: String
)

fun Route.getGroceries(database: Database) {
    val db = database.getGroceriesQueries

    authenticate {
        get("/apartment/groceries") {
            val session = call.principal<UserSessionPrincipal>()!!

            val result = db.getApartmentIdByUserId(session.userId).executeAsOneOrNull()
                ?: throw UnauthorizedException("Account no longer exists")
            val apartmentId = result.apartment_id
                ?: throw NotFoundException("You must be in an apartment to view groceries.")

            val groceries = db.getGroceriesFromApartment(apartmentId)
                .executeAsList()
                .map {
                    Grocery(
                        id = it.id,
                        name = it.name,
                        addedByUsername = it.addedByUsername
                    )
                }

            call.respond(HttpStatusCode.OK, groceries)
        }
    }
}