package dev.jakobdario

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

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

fun initialiseDatabase() {
    runBlocking {
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
    }


    //test data, comment/uncomment if needed/not needed
    /*runBlocking {
        SqliteDatabase.executeUpdate(
            """
                insert into users (email, username, password_hash)
                values (?, ?, ?)
            """.trimIndent()
        ) {
            setString(1, "test@gmail.com")
            setString(2, "test")
            setString(3, "test")
        }
    }*/
}
