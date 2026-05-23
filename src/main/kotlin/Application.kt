package dev.jakobdario

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import configureRouting
import features.auth.configureAuth
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
import kotlinx.serialization.Serializable
import shared.UnauthorizedException
import shared.validation.ValidationException
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
    configureRouting(database)
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

@Serializable
data class ErrorResponse(val error: String)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is ValidationException -> call.respond(HttpStatusCode.BadRequest,
                    ErrorResponse(cause.message ?: "Validation error"))
                is UnauthorizedException -> call.respond(HttpStatusCode.Unauthorized,
                    ErrorResponse(cause.message ?: "Unauthorized"))
                is ContentTransformationException -> call.respond(HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid request format: ${cause.message}"))

                else -> call.respond(HttpStatusCode.InternalServerError, mapOf("error" to cause.message))
            }
        }
    }
}
