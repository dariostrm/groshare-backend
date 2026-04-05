package dev.jakobdario.repositories

import dev.jakobdario.SqliteDatabase
import dev.jakobdario.User
import java.sql.ResultSet

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun addUser(user: User, password: String)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: Int)
}

private fun ResultSet.toUser(): User {
    return User(
        id = this.getInt("id"),
        email = this.getString("email"),
        username = this.getString("username")
    )
}

class UserRepositorySqlite() : UserRepository {

    override suspend fun getUsers(): List<User> =
        SqliteDatabase.executeQuery("SELECT * FROM users", map = ResultSet::toUser)


    override suspend fun getUserById(id: Int): User? {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM users WHERE id = ?",
            map = ResultSet::toUser
        ) {
            setInt(1, id)
        }.firstOrNull()
    }

    override suspend fun getUserByUsername(username: String): User? {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM users WHERE username = ?",
            map = ResultSet::toUser
        ) {
            setString(1, username)
        }.firstOrNull()
    }

    override suspend fun getUserByEmail(email: String): User? {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM users WHERE email = ?",
            map = ResultSet::toUser
        ) {
            setString(1, email)
        }.firstOrNull()
    }

    override suspend fun addUser(user: User, password: String) {
        SqliteDatabase.executeUpdate(
            "INSERT INTO users (email, username, password_hash) VALUES (?, ?, ?)"
        ) {
            setString(1, user.email)
            setString(2, user.username)
            setString(3, password)
        }
    }

    override suspend fun updateUser(user: User) {
        SqliteDatabase.executeUpdate(
            "UPDATE users SET email = ?, username = ? WHERE id = ?"
        ) {
            setString(1, user.email)
            setString(2, user.username)
            setInt(3, user.id)
        }
    }

    override suspend fun deleteUser(id: Int) {
        SqliteDatabase.executeUpdate(
            "DELETE FROM users WHERE id = ?"
        ) {
            setInt(1, id)
        }
    }
}