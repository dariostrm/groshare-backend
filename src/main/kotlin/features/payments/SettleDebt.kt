package dev.jakobdario.features.payments

import dev.jakobdario.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import shared.ForbiddenException
import shared.UserSessionPrincipal
import shared.validation.ensure
import kotlin.time.Clock

@Serializable
data class SettleDebtRequest(val recipientId: Long, val amountInCents: Long) {
    fun sanitizeAndEnsureValid(currentUserId: Long): SettleDebtRequest {
        ensure(amountInCents > 0) { "The amount must be greater than 0" }
        ensure(recipientId > 0) { "The recipient id must be greater than 0" }
        ensure(recipientId != currentUserId) { "You cannot settle debt with yourself" }
        return this
    }
}

fun Route.settleDebt(database: Database) {
    val db = database.settleDebtQueries
    authenticate {
        post("/apartment/debts/settle") {
            val session = call.principal<UserSessionPrincipal>()!!
            val request = call.receive<SettleDebtRequest>().sanitizeAndEnsureValid(session.userId)

            db.transaction {
                db.checkAreRoommates(session.userId, request.recipientId)
                    .executeAsOneOrNull()
                    ?: throw ForbiddenException("Recipient and you are not in the same apartment")

                db.insertPayment(
                    userId = session.userId,
                    recipientId = request.recipientId,
                    amountInCents = request.amountInCents,
                    timeStamp = Clock.System.now().epochSeconds
                ).executeAsOne()
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}