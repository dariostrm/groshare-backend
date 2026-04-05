package dev.jakobdario

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.module() {

    initialiseDatabase()
    configureSerialization()
    configureRouting()
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

suspend fun initialiseDatabase() {
    SqliteDatabase.executeUpdate(
        """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email VARCHAR(255) NOT NULL UNIQUE,
            username TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL
        )
        """.trimIndent()
    ) {}

    //test data, comment out after first run to avoid duplicates
    val sql = """
            insert into users (email, username, password_hash)
            values (?, ?, ?)
        """.trimIndent()
    /*SqliteDatabase.executeUpdate(sql) {
        setString(1, "test@gmail.com")
        setString(2, "test")
        setString(3, "test")
    }*/
}
