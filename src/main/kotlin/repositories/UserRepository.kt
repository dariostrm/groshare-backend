/*
package dev.jakobdario.repositories

import dev.jakobdario.auth.PasswordHash
import dev.jakobdario.entities.UserEntity
import java.sql.ResultSet

interface UserRepository {
    suspend fun getUsers(): List<UserEntity>
    suspend fun getUserById(id: Int): UserEntity?
    suspend fun getUserByUsername(username: String): UserEntity?
    suspend fun updateUser(user: dev.jakobdario.domain.entities.UserEntity)
    suspend fun deleteUser(id: Int)
    suspend fun checkUniqueUsername(username: String): Boolean
    suspend fun checkUniqueEmail(email: String): Boolean
    suspend fun checkPassword(userId: Int, check: (PasswordHash) -> Boolean): Boolean
    suspend fun signUp(username: String, email: String, hashedPassword: PasswordHash) : UserEntity
}

fun ResultSet.toUser(): UserEntity {
    return UserEntity(
        id = this.getInt("id"),
        email = this.getString("email"),
        username = this.getString("username"),
        apartmentId = try { this.getInt("apartment_id") } catch (_: Exception) { null }
    )
}

class UserRepositorySqlite() : UserRepository {

    override suspend fun getUsers(): List<UserEntity> =
        SqliteDatabase.executeQuery("SELECT * FROM users", map = ResultSet::toUser)


    override suspend fun getUserById(id: Int): UserEntity? {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM users WHERE id = ?",
            map = ResultSet::toUser
        ) {
            setInt(1, id)
        }.firstOrNull()
    }

    override suspend fun getUserByUsername(username: String): UserEntity? {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM users WHERE username = ?",
            map = ResultSet::toUser
        ) {
            setString(1, username)
        }.firstOrNull()
    }

    override suspend fun updateUser(user: dev.jakobdario.domain.entities.UserEntity) {
        SqliteDatabase.executeUpdate(
            "UPDATE users SET email = ?, username = ? WHERE id = ?"
        ) {
            setString(1, user.email)
            setString(2, user.username)
            if (user.apartmentId == null)
                setString(3, user.apartmentId)
            else setNull(3, java.sql.Types.INTEGER)
            setInt(4, user.id)
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

    override suspend fun checkPassword(userId: Int, check: (PasswordHash) -> Boolean): Boolean {
        val passwordHash = SqliteDatabase.executeQuery(
            "SELECT password_hash FROM users WHERE id = ?",
            map = { getString("password_hash") }
        ) {
            setInt(1, userId)
        }.firstOrNull() ?: return false
        return check(PasswordHash(passwordHash))
    }

    override suspend fun signUp(username: String, email: String, hashedPassword: PasswordHash) : UserEntity {
        SqliteDatabase.executeUpdate(
            "INSERT INTO users (email, username, password_hash) VALUES (?, ?, ?)"
        ) {
            setString(1, email)
            setString(2, username)
            setString(3, hashedPassword.value)
        }
        return getUserByUsername(username) ?: throw IllegalStateException("User was not created")
    }
}*/
