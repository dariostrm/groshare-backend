package dev.jakobdario.features.payments

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import shared.NotFoundException
import shared.UnauthorizedException
import shared.UserSessionPrincipal

@Serializable
data class Roommate(val id: Long, val username: String)

@Serializable
data class Debt(
    val roommate: Roommate,
    val amountCents: Long
)

@Serializable
data class ApartmentDebtsResponse(
    val totalNetCents: Long,
    val debts: List<Debt>
)

fun Route.getDebts(database: Database) {
    val db = database.getDebtsQueries
    authenticate {
        get("/apartment/debts") {
            val session = call.principal<UserSessionPrincipal>()!!

            val apartmentResult = db.getApartmentIdByUserId(session.userId).executeAsOneOrNull()
                ?: throw UnauthorizedException("Account no longer exists")
            val apartmentId = apartmentResult.apartment_id
                ?: throw NotFoundException("You are not in any apartment")

            val balances = db.getRoommateBalances(
                myUserId = session.userId,
                aptId = apartmentId
            ).executeAsList()

            var totalNet = 0L

            val debts = balances.map { row ->
                val netBalance = (row.they_owe_me - row.i_owe_them).toLong()
                totalNet += netBalance

                Debt(
                    roommate = Roommate(row.roommate_id, row.roommate_username),
                    amountCents = netBalance
                )
            }

            call.respond(
                HttpStatusCode.OK,
                ApartmentDebtsResponse(totalNet, debts)
            )
        }
    }
}