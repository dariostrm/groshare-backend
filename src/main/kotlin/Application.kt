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
    val db = DriverManager.getConnection(dbConnectionUrl)

    initialiseDatabase(db)
    configureSerialization()
    configureRouting(db)
    monitor.subscribe(ApplicationStopped) {
        db.close()
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun initialiseDatabase(db: java.sql.Connection) {
    db.createStatement().use { statement ->
        statement.executeUpdate(
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

    //test data, comment out after first run to avoid duplicates
    val sql = """
            insert into users (email, username, password_hash)
            values (?, ?, ?)
        """.trimIndent()
    db.prepareStatement(sql).use { statement ->
        statement.setString(1, "test@gmail.com")
        statement.setString(2, "test")
        statement.setString(3, "test")
        statement.executeUpdate()
    }
    db.prepareStatement(sql).use { statement ->
        statement.setString(1, "jakob@gmail.com")
        statement.setString(2, "jakob")
        statement.setString(3, "test")
        statement.executeUpdate()
    }
    db.prepareStatement(sql).use { statement ->
        statement.setString(1, "dario@gmail.com")
        statement.setString(2, "dario")
        statement.setString(3, "test")
        statement.executeUpdate()
    }
}
