package com.example.services

import com.example.models.User
import com.example.services.UserService.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserService(database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50).uniqueIndex()
        val email = varchar("email", length = 255).uniqueIndex()
        val passwordHash = varchar("password_hash", length = 64)
        val uid = varchar("uid", length = 255).uniqueIndex()
        val createdAt = datetime("created_at").clientDefault { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
        val updatedAt = datetime("updated_at")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(user: User): Int = dbQuery {
        val hashedPassword = BCrypt.hashpw(user.passwordHash.orEmpty(), BCrypt.gensalt())
        Users.insert {
            it[name] = user.name
            it[email] = user.email
            it[passwordHash] = hashedPassword
            it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            it[updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            it[uid] = user.uid
        }[Users.id]
    }

    suspend fun read(id: Int): User? {
        return dbQuery {
            Users.selectAll()
                .where { Users.id eq id }
                .map { User(
                    id = it[Users.id],
                    name = it[Users.name],
                    email = it[Users.email],
                    passwordHash = it[Users.passwordHash],
                    createdAt = it[Users.createdAt].toString(),
                    updatedAt = it[Users.updatedAt].toString(),
                    uid = it[Users.uid]
                ) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: User) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[email] = user.email
                it[passwordHash] = user.passwordHash.orEmpty()
                it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                it[updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    suspend fun authenticate(email: String, passwordHash: String): User? {
        return dbQuery {
            Users.selectAll().where { Users.email eq email }
                .mapNotNull {
                    val user = User(
                        id = it[Users.id],
                        name = it[Users.name],
                        email = it[Users.email],
                        passwordHash = it[Users.passwordHash],
                        createdAt = it[Users.createdAt].toString(),
                        updatedAt = it[Users.updatedAt].toString(),
                        uid = it[Users.uid]
                    )
                    if (BCrypt.checkpw(passwordHash, user.passwordHash)) user else null
                }
                .singleOrNull()
        }
    }

    suspend fun findByEmail(email: String): User? {
        return dbQuery {
            Users.selectAll().where { Users.email eq email }
                .map { User(
                    id = it[Users.id],
                    name = it[Users.name],
                    email = it[Users.email],
                    passwordHash = it[Users.passwordHash],
                    createdAt = it[Users.createdAt].toString(),
                    updatedAt = it[Users.updatedAt].toString(),
                    uid = it[Users.uid]
                ) }
                .singleOrNull()
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

