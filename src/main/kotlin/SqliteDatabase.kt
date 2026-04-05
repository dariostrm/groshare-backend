package dev.jakobdario

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

object SqliteDatabase {
    val connection: Connection = DriverManager.getConnection("jdbc:sqlite:groshare_backend.db")

    //only one request can use the sqlite database at the same time
    //this is enough for a school project, introducing connection pools would be overkill
    private val dbDispatcher = Dispatchers.IO.limitedParallelism(1)

    suspend fun <T> execute(block: Connection.() -> T): T {
        return withContext(dbDispatcher) {
            connection.block()
        }
    }

    suspend fun <T> executePrepared(
        @Language("SQLite") sql: String,
        prepare: PreparedStatement.() -> Unit = {},
        block: PreparedStatement.() -> T
    ): T {
        return withContext(dbDispatcher) {
            connection.prepareStatement(sql).use {
                it.prepare()
                it.block()
            }
        }
    }

    suspend fun <T> executeQuery(
        @Language("SQLite") sql: String,
        map: ResultSet.() -> T,
        prepare: PreparedStatement.() -> Unit = {}
    ): List<T> {
        return executePrepared(sql, prepare) {
            val resultSet = this.executeQuery()
            val list = mutableListOf<T>()
            while (resultSet.next()) {
                list.add(resultSet.map())
            }
            list
        }
    }

    suspend fun executeUpdate(
        @Language("SQLite") sql: String,
        prepare: PreparedStatement.() -> Unit = {}
    ): Int {
        return executePrepared(sql, prepare) {
            this.executeUpdate()
        }
    }

    fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}