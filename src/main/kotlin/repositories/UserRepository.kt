package dev.jakobdario.repositories

import dev.jakobdario.User
import java.sql.DriverManager

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun addUser(user: User, password: String)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: Int)
}

class UserRepositorySqlite(private val connectionUrl: String) : UserRepository {
    private fun java.sql.ResultSet.toUser(): User {
        return User(
            id = this.getInt("id"),
            email = this.getString("email"),
            username = this.getString("username")
        )
    }

    override suspend fun getUsers(): List<User> {
        DriverManager.getConnection(connectionUrl).use { connection ->
            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery("SELECT * FROM users")
                val users = mutableListOf<User>()
                while (resultSet.next()) {
                    users.add(resultSet.toUser())
                }
                return users
            }
        }
    }

    override suspend fun getUserById(id: Int): User? {
        DriverManager.getConnection(connectionUrl).use { connection ->
            val sql = "SELECT * FROM users WHERE id = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, id)
                val resultSet = statement.executeQuery()
                return if (resultSet.next()) resultSet.toUser() else null
            }
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        DriverManager.getConnection(connectionUrl).use { connection ->
            val sql = "SELECT * FROM users WHERE username = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, username)
                val resultSet = statement.executeQuery()
                return if (resultSet.next()) resultSet.toUser() else null
            }
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        DriverManager.getConnection(connectionUrl).use { connection ->
            val sql = "SELECT * FROM users WHERE email = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, email)
                val resultSet = statement.executeQuery()
                return if (resultSet.next()) resultSet.toUser() else null
            }
        }
    }

    override suspend fun addUser(user: User, password: String) {
        DriverManager.getConnection(connectionUrl).use { connection ->
            val sql = "INSERT INTO users (email, username, password_hash) VALUES (?, ?, ?)"
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, user.email)
                statement.setString(2, user.username)
                statement.setString(3, password)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun updateUser(user: User) {
        DriverManager.getConnection(connectionUrl).use { connection ->
            val sql = "UPDATE users SET email = ?, username = ? WHERE id = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, user.email)
                statement.setString(2, user.username)
                statement.setInt(3, user.id)
                statement.executeUpdate()
            }
        }
    }

    override suspend fun deleteUser(id: Int) {
        DriverManager.getConnection(connectionUrl).use { connection ->
            val sql = "DELETE FROM users WHERE id = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, id)
                statement.executeUpdate()
            }
        }
    }

}