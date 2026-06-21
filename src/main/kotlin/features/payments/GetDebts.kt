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
enum class DebtRelation { OWES_ME, I_OWE, FLAT }

@Serializable
data class Roommate(val id: Long, val username: String)

@Serializable
data class Debt(
    val roommate: Roommate,
    val amountCents: Int,
    val relation: DebtRelation
)

@Serializable
data class ApartmentDebtsResponse(
    val totalNetCents: Int,
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

            var totalNet = 0

            val debts = balances.map { row ->
                val netBalance = (row.they_owe_me - row.i_owe_them).toInt()
                totalNet += netBalance

                val relation = when {
                    netBalance > 0 -> DebtRelation.OWES_ME
                    netBalance < 0 -> DebtRelation.I_OWE
                    else -> DebtRelation.FLAT
                }

                Debt(
                    roommate = Roommate(row.roommate_id, row.roommate_username),
                    amountCents = kotlin.math.abs(netBalance),
                    relation = relation
                )
            }

            call.respond(
                HttpStatusCode.OK,
                ApartmentDebtsResponse(totalNet, debts)
            )
        }
    }
}