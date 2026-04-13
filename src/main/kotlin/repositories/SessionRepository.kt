package dev.jakobdario.repositories

import dev.jakobdario.SqliteDatabase
import java.util.Date
import java.util.UUID

interface SessionRepository {
    suspend fun getUserId(sessionId: UUID): Int?
    suspend fun createSession(userId: Int) : UUID
    suspend fun deleteSession(sessionId: UUID)
    suspend fun deleteAllSessions(userId: Int)
}

class SessionRepositorySqlite(val tokenExpiration: Long = 60 * 60 * 24 * 30L) : SessionRepository {

    private fun Date.toUnixTimestamp(): Long {
        return time / 1000
    }

    override suspend fun getUserId(sessionId: UUID): Int? {
        return SqliteDatabase.executeQuery(
            "SELECT user_id FROM sessions WHERE sessions.id = ? AND sessions.expires_at > ?",
             map = { getInt("user_id") }
        ) {
            setString(1, sessionId.toString())
            setLong(2, Date().toUnixTimestamp())
        }.firstOrNull()
    }

    override suspend fun createSession(userId: Int): UUID {
        val sessionId = UUID.randomUUID()
        SqliteDatabase.executeUpdate(
            "INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)"
        ) {
            setString(1, sessionId.toString())
            setInt(2, userId)
            setLong(3, Date().toUnixTimestamp() + tokenExpiration)
        }
        return sessionId
    }

    override suspend fun deleteSession(sessionId: UUID) {
        SqliteDatabase.executeUpdate(
            "DELETE FROM sessions WHERE id = ?"
        ) {
            setString(1, sessionId.toString())
        }
    }

    override suspend fun deleteAllSessions(userId: Int) {
        SqliteDatabase.executeUpdate(
            "DELETE FROM sessions WHERE user_id = ?"
        ) {
            setInt(1, userId)
        }
    }

}