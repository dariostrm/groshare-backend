package dev.jakobdario

import dev.jakobdario.repositories.SessionRepository
import dev.jakobdario.repositories.SessionRepositorySqlite
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.coroutines.runBlocking
import java.util.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val sessionRepo = SessionRepositorySqlite()

    configureCors()
    configureDatabase()
    configureSerialization()
    configureAuth(sessionRepo)
    configureRouting(sessionRepo)
    monitor.subscribe(ApplicationStopped) {
        println("Closing database...")
        SqliteDatabase.close()
    }
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
    val userId: Int,
    val sessionId: UUID
)
fun Application.configureAuth(sessionRepository: SessionRepository) {
    install(Authentication) {
        bearer {
            realm = "GroShare"
            authenticate { tokenCredential ->
                val sessionId = try {
                    UUID.fromString(tokenCredential.token)
                } catch (e: IllegalArgumentException) {
                    return@authenticate null
                }
                val userId = sessionRepository.getUserId(sessionId)
                if (userId != null)
                    UserSessionPrincipal(userId, sessionId)
                else null
            }
        }
    }
}

fun configureDatabase() {
    runBlocking {
        SqliteDatabase.executeUpdate("PRAGMA foreign_keys = ON;")

        SqliteDatabase.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email VARCHAR(255) NOT NULL UNIQUE,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL
            )
            """.trimIndent()
        )
        SqliteDatabase.executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS sessions (
                id VARCHAR(255) PRIMARY KEY,
                user_id INT NOT NULL,
                expires_at INTEGER NOT NULL, -- Unix Time (number of seconds since 1970-01-01)
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}
