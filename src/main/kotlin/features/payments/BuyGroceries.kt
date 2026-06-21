package dev.jakobdario.features.payments

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import shared.ForbiddenException
import shared.NotFoundException
import shared.UnauthorizedException
import shared.UserSessionPrincipal
import shared.validation.ensure
import kotlin.time.Clock

@Serializable
data class GroceryPurchase(val id: Long, val priceInCents: Long) {
    fun sanitizeAndEnsureValid(): GroceryPurchase {
        ensure(priceInCents > 0) { "The price must be greater than 0" }
        ensure(id > 0) { "The id must be greater than 0" }
        return this
    }
}

@Serializable
data class BuyGroceriesRequest(val purchases: List<GroceryPurchase>) {
    fun sanitizeAndEnsureValid(): BuyGroceriesRequest {
        ensure(purchases.isNotEmpty()) { "At least one purchase must be provided" }
        ensure(purchases.distinctBy { it.id }.size == purchases.size) { "Duplicate grocery ids are not allowed" }
        return BuyGroceriesRequest(purchases.map { it.sanitizeAndEnsureValid() })
    }
}

fun Route.buyGroceries(database: Database) {
    val db = database.buyGroceriesQueries

    authenticate {
        post("/apartment/groceries/buy") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<BuyGroceriesRequest>().sanitizeAndEnsureValid()

            db.transaction {
                val apartmentResult = db.getApartmentIdByUserId(session.userId).executeAsOneOrNull()
                    ?: throw UnauthorizedException("Account no longer exists")
                val apartmentId = apartmentResult.apartment_id
                    ?: throw NotFoundException("You are not in any apartment")

                val timeStamp = Clock.System.now().epochSeconds
                val affectedGroceries = db.updateBoughtGroceries(
                    timeStamp,
                    apartmentId,
                    request.purchases.map { it.id }
                ).executeAsList()
                if (affectedGroceries.size != request.purchases.size)
                    throw ForbiddenException("Some groceries could not be updated")

                val priceMap = request.purchases.associateBy({ it.id }, { it.priceInCents })
                affectedGroceries.forEach { grocery ->
                    if (grocery.creator_id != session.userId) {
                        db.insertPayment(
                            buyerId = session.userId,
                            itemOwnerId = grocery.creator_id,
                            priceInCents = priceMap.getValue(grocery.id),
                            grocery = grocery.id,
                            timeStamp = timeStamp
                        ).executeAsOne()
                    }
                }
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}