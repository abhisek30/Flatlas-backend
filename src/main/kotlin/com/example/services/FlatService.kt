package com.example.services

import com.example.models.Amenity
import com.example.models.Flat
import com.example.models.Images
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.json.jsonb

val format = Json { prettyPrint = true }

class FlatService(database: Database) {
    object Flats : Table() {
        val id = integer("id").autoIncrement()
        val address = varchar("address", length = 255)
        val price = double("price")
        val details = text("details")
        val latitude = double("latitude")
        val longitude = double("longitude")
        val contactInfo = text("contact_info")
        val amenities = jsonb("amenities", format, Amenity.serializer())
        val images = jsonb("images", format, Images.serializer())
        val createdAt =
            datetime("created_at").clientDefault { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
        val updatedAt = datetime("updated_at")
        val userId = integer("user_id").references(UserService.Users.id)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Flats)
        }
    }

    suspend fun create(flat: Flat): Int = dbQuery {
        Flats.insert {
            it[address] = flat.address
            it[price] = flat.price
            it[details] = flat.details
            it[latitude] = flat.latitude
            it[longitude] = flat.longitude
            it[contactInfo] = flat.contactInfo
            it[amenities] = Amenity(flat.amenities)
            it[images] = Images(flat.images)
            it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            it[updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            it[userId] = flat.userId
        }[Flats.id]
    }

    suspend fun read(id: Int): Flat? {
        return dbQuery {
            Flats.selectAll().where { Flats.id eq id }
                .map {
                    Flat(
                        it[Flats.id],
                        it[Flats.address],
                        it[Flats.price],
                        it[Flats.details],
                        it[Flats.latitude],
                        it[Flats.longitude],
                        it[Flats.contactInfo],
                        it[Flats.amenities].map,
                        it[Flats.images].urls,
                        it[Flats.createdAt].toString(),
                        it[Flats.updatedAt].toString(),
                        it[Flats.userId]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, flat: Flat) {
        dbQuery {
            Flats.update({ Flats.id eq id }) {
                it[address] = flat.address
                it[price] = flat.price
                it[details] = flat.details
                it[latitude] = flat.latitude
                it[longitude] = flat.longitude
                it[contactInfo] = flat.contactInfo
                it[amenities] = Amenity(flat.amenities)
                it[images] = Images(flat.images)
                it[updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Flats.deleteWhere { Flats.id eq id }
        }
    }

    suspend fun getAllFlats(): List<Flat> = dbQuery {
        Flats.selectAll().map {
            Flat(
                it[Flats.id],
                it[Flats.address],
                it[Flats.price],
                it[Flats.details],
                it[Flats.latitude],
                it[Flats.longitude],
                it[Flats.contactInfo],
                it[Flats.amenities].map,
                it[Flats.images].urls,
                it[Flats.createdAt].toString(),
                it[Flats.updatedAt].toString(),
                it[Flats.userId]
            )
        }
    }

    suspend fun searchFlatsNearby(latitude: Double, longitude: Double, radius: Double): List<Flat> = dbQuery {
        Flats.selectAll().map {
            Flat(
                it[Flats.id],
                it[Flats.address],
                it[Flats.price],
                it[Flats.details],
                it[Flats.latitude],
                it[Flats.longitude],
                it[Flats.contactInfo],
                it[Flats.amenities].map,
                it[Flats.images].urls,
                it[Flats.createdAt].toString(),
                it[Flats.updatedAt].toString(),
                it[Flats.userId]
            )
        }.filter {
            val distance = haversine(latitude, longitude, it.latitude, it.longitude)
            distance <= radius
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6372.8 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.asin(Math.sqrt(a))
        return R * c
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}