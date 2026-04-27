package dev.jakobdario.profile

import dev.jakobdario.User
import dev.jakobdario.getSession
import dev.jakobdario.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Profile(val username: String, val email: String)

fun Route.profileRoutes(
    userRepository: UserRepository,
) {
    authenticate {
        get("/profile") {
            val user = userRepository.getUserById(getSession().userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "The user was not found")
            call.respond(HttpStatusCode.OK, Profile(user.username, user.email))
        }
        put("/profile") {
            val userId = getSession().userId
            val currentUser = userRepository.getUserById(userId)
                ?: return@put call.respond(HttpStatusCode.NotFound, "Your user was not found")
            val request = try {
                call.receive<Profile>()
            } catch (e: Exception) {
                return@put call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            }

            if (currentUser.username != request.username &&
                !userRepository.checkUniqueUsername(request.username)) {
                return@put call.respond(HttpStatusCode.Conflict, "Username is already taken")
            }
            if (currentUser.email != request.email &&
                !userRepository.checkUniqueEmail(request.email)) {
                return@put call.respond(HttpStatusCode.Conflict, "Email is already taken")
            }

            userRepository.updateUser(User(
                id = userId,
                username = request.username,
                email = request.email
            ))
            call.respond(HttpStatusCode.OK, "Profile updated successfully")
        }
    }
}