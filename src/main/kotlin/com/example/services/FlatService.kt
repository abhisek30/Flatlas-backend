package com.example.services

import com.example.models.Flat
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class FlatService(database: Database) {
    object Flats : Table() {
        val id = integer("id").autoIncrement()
        val address = varchar("address", length = 255)
        val price = double("price")
        val details = text("details")
        val latitude = double("latitude")
        val longitude = double("longitude")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Flats)
        }
    }

    suspend fun create(flat: Flat): Int = dbQuery {
        Flats.insert {
            it[address] = flat.address
            it[price] = flat.price
            it[details] = flat.details
            it[latitude] = flat.latitude
            it[longitude] = flat.longitude
        }[Flats.id]
    }

    suspend fun read(id: Int): Flat? {
        return dbQuery {
            Flats.selectAll().where { Flats.id eq id }
                .map { Flat(it[Flats.id], it[Flats.address], it[Flats.price], it[Flats.details], it[Flats.latitude], it[Flats.longitude]) }
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
            Flat(it[Flats.id], it[Flats.address], it[Flats.price], it[Flats.details], it[Flats.latitude], it[Flats.longitude])
        }
    }

    suspend fun searchFlatsNearby(latitude: Double, longitude: Double, radius: Double): List<Flat> = dbQuery {
        Flats.selectAll().map {
            Flat(it[Flats.id], it[Flats.address], it[Flats.price], it[Flats.details], it[Flats.latitude], it[Flats.longitude])
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