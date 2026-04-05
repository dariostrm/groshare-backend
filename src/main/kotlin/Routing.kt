package dev.jakobdario

import dev.jakobdario.repositories.UserRepository
import dev.jakobdario.repositories.UserRepositorySqlite
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(db: java.sql.Connection) {
    val userRepository : UserRepository = UserRepositorySqlite(db)
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/test") {
            call.respondText("Test Application!")
        }
        get("/users") {
            val users = userRepository.getUsers()
            call.respond(users)
        }
    }
}
