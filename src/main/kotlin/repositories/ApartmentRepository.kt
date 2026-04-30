package dev.jakobdario.repositories

import dev.jakobdario.SqliteDatabase
import dev.jakobdario.auth.Hash
import dev.jakobdario.entities.ApartmentEntity
import dev.jakobdario.entities.UserEntity
import java.sql.ResultSet

interface ApartmentRepository {
    suspend fun createApartment(name: String, address: String, city: String): Int
    suspend fun getApartmentById(id: Int): ApartmentEntity?
    suspend fun updateApartment(apartment: ApartmentEntity)
    suspend fun deleteApartment(id: Int)

    suspend fun addUser(userId: Int, apartmentId: Int)
    suspend fun getUsers(apartmentId: Int): List<UserEntity>
    suspend fun removeUser(userId: Int)
}

fun ResultSet.toApartment(): ApartmentEntity {
    return ApartmentEntity(
        id = this.getInt("id"),
        name = this.getString("name"),
        address = this.getString("address"),
        city = this.getString("city")
    )
}

class ApartmentRepositorySqlite() : ApartmentRepository {

    override suspend fun createApartment(
        name: String,
        address: String,
        city: String
    ): Int {
        var insertedId: Int? = null
        SqliteDatabase.executeUpdate(
            "INSERT INTO apartments (name, address, city) VALUES (?, ?, ?)"
        ) {
            setString(1, name)
            setString(2, address)
            setString(3, city)
            if (generatedKeys.next()) {
                insertedId = generatedKeys.getInt(1)
            } else {
                throw Exception("Inserting failed, no ID obtained.")
            }
        }
        return insertedId ?: throw IllegalStateException("Apartment could not be created")
    }

    override suspend fun getApartmentById(id: Int): ApartmentEntity? {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM apartments WHERE id = ?",
            map = ResultSet::toApartment
        ) {
            setInt(1, id)
        }.firstOrNull()
    }

    override suspend fun updateApartment(apartment: ApartmentEntity) {
        SqliteDatabase.executeUpdate(
            "UPDATE apartments SET name = ?, address = ?, city = ? WHERE id = ?"
        ) {
            setString(1, apartment.name)
            setString(2, apartment.address)
            setString(3, apartment.city)
            setInt(4, apartment.id)
        }
    }

    override suspend fun deleteApartment(id: Int) {
        SqliteDatabase.executeUpdate(
            "DELETE FROM apartments WHERE id = ?"
        ) {
            setInt(1, id)
        }
    }

    override suspend fun addUser(userId: Int, apartmentId: Int) {
        SqliteDatabase.executeUpdate(
            "UPDATE users SET apartment_id = ? WHERE id = ?"
        ) {
            setInt(1, apartmentId)
            setInt(2, userId)
        }
    }

    override suspend fun getUsers(apartmentId: Int): List<UserEntity> {
        return SqliteDatabase.executeQuery(
            "SELECT * FROM users WHERE apartment_id = ?",
            map = ResultSet::toUser
        ) {
            setInt(1, apartmentId)
        }
    }

    override suspend fun removeUser(userId: Int) {
        SqliteDatabase.executeUpdate(
            "UPDATE users SET apartment_id = NULL WHERE id = ?"
        ) {
            setInt(1, userId)
        }
    }
}