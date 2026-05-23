import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import auth.configureAuth
import dev.jakobdario.database.Database
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.respond
import shared.UnauthorizedException
import shared.ValidationException
import java.util.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val database = configureDatabase()
    configureCors()
    configureErrorHandling()
    configureSerialization()
    configureAuth(database)
    configureRouting()

}

fun Application.configureDatabase(): Database {
    val dbUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:sqlite:groshare_backend.db"
    val driver: SqlDriver = JdbcSqliteDriver(
        url = dbUrl,
        properties = Properties().apply {
            put("foreign_keys", "true")
            put("journal_mode", "WAL")
            put("busy_timeout", "5000")
        },
        schema = Database.Schema,
        migrateEmptySchema = true
    )
    monitor.subscribe(ApplicationStopped) {
        println("Closing database...")
        driver.close()
    }
    return Database(driver)
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureCors() {
    install(CORS) {
        anyHost()

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowNonSimpleContentTypes = true
        allowCredentials = true
    }
}

data class UserSessionPrincipal(
    val userId: Long,
    val sessionId: UUID
)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is ValidationException -> call.respond(HttpStatusCode.BadRequest,
                    mapOf("error" to cause.message))
                is UnauthorizedException -> call.respond(HttpStatusCode.Unauthorized,
                    mapOf("error" to cause.message))
                is ContentTransformationException -> call.respond(HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request body format"))

                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("error" to cause.message))
            }
        }
    }
}
