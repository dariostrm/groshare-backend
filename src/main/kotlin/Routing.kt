import auth.authRoutes
import dev.jakobdario.database.Database
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(database: Database) {

    routing {
        route("/api/v1") {
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml") {

            }
            get {
                call.respondText("Hello World!")
            }
            authRoutes(database)
        }
    }
}
