package dev.jakobdario

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import java.sql.DriverManager

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val dbConnectionUrl = "jdbc:sqlite:groshare.db"

    initialiseDatabase(dbConnectionUrl)
    configureSerialization()
    configureRouting(dbConnectionUrl)
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
fun initialiseDatabase(dbConnectionUrl: String) {
    DriverManager.getConnection(dbConnectionUrl).use { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email VARCHAR(255) NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL
                )
                """.trimIndent()
            )
        }

        //test data, comment out the return if you need it
        val sql = """
            insert into users (email, username, password_hash)
            values (?, ?, ?)
        """.trimIndent()
        return
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, "test@gmail.com")
            statement.setString(2, "test")
            statement.setString(3, "test")
            statement.executeUpdate()
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, "jakob@gmail.com")
            statement.setString(2, "jakob")
            statement.setString(3, "test")
            statement.executeUpdate()
        }
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, "dario@gmail.com")
            statement.setString(2, "dario")
            statement.setString(3, "test")
            statement.executeUpdate()
        }
    }
}
