package dev.jakobdario.repositories

import dev.jakobdario.SqliteDatabase
import dev.jakobdario.User
import dev.jakobdario.auth.Hash
import java.sql.ResultSet

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: Int)
    suspend fun checkUniqueUsername(username: String): Boolean
    suspend fun checkUniqueEmail(email: String): Boolean
    suspend fun checkPassword(userId: Int, check: (Hash) -> Boolean): Boolean
    suspend fun signUp(username: String, email: String, hashedPassword: Hash) : User
}

fun ResultSet.toUser(): User {
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

    override suspend fun checkUniqueUsername(username: String): Boolean {
        return SqliteDatabase.executeQuery(
            "SELECT id FROM users WHERE username = ?",
            map = { getInt("id") }
        ) {
            setString(1, username)
        }.isEmpty()
    }

    override suspend fun checkUniqueEmail(email: String): Boolean {
        return SqliteDatabase.executeQuery(
            "SELECT id FROM users WHERE email = ?",
            map = { getInt("id") }
        ) {
            setString(1, email)
        }.isEmpty()
    }

    override suspend fun checkPassword(userId: Int, check: (Hash) -> Boolean): Boolean {
        val passwordHash = SqliteDatabase.executeQuery(
            "SELECT password_hash FROM users WHERE id = ?",
            map = { getString("password_hash") }
        ) {
            setInt(1, userId)
        }.firstOrNull() ?: return false
        return check(Hash(passwordHash))
    }

    override suspend fun signUp(username: String, email: String, hashedPassword: Hash) : User {
        SqliteDatabase.executeUpdate(
            "INSERT INTO users (email, username, password_hash) VALUES (?, ?, ?)"
        ) {
            setString(1, email)
            setString(2, username)
            setString(3, hashedPassword.value)
        }
        return getUserByUsername(username) ?: throw IllegalStateException("User was not created")
    }
}