package com.adrianobispo

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase

@Serializable
/**
 * Serializable representation of a user stored in the database.
 */
data class ExposedUser(val name: String, val age: Int)

/**
 * Service responsible for creating the user schema and performing CRUD operations.
 *
 * @property database The Exposed R2DBC database used for all transactions.
 */
class ExposedUserService(val database: R2dbcDatabase) {
    /**
     * Database table definition for users.
     */
    object Users : UIntIdTable() {
        val name = varchar("name", length = 50)
        val age = integer("age")
    }

    /**
     * Creates the users table if it does not already exist.
     */
    suspend fun createSchema() {
        suspendTransaction(database) {
            SchemaUtils.create(Users)
        }
    }

    /**
     * Inserts a new user record and returns its generated identifier.
     *
     * @param user The user data to persist.
     * @return The generated user ID.
     */
    suspend fun create(user: ExposedUser): UInt = suspendTransaction(database) {
        val newRecord = Users.insert {
            it[name] = user.name
            it[age] = user.age
        }
        newRecord[Users.id].value
    }

    /**
     * Loads a user by identifier.
     *
     * @param id The user ID to look up.
     * @return The matching user, or `null` when no record exists.
     */
    suspend fun read(id: UInt): ExposedUser? {
        return suspendTransaction(database) {
            Users.selectAll()
                .where { Users.id eq id }
                .map { ExposedUser(it[Users.name], it[Users.age]) }
                .singleOrNull()
        }
    }

    /**
     * Updates an existing user record with the provided values.
     *
     * @param id The user ID to update.
     * @param user The new user values.
     */
    suspend fun update(id: UInt, user: ExposedUser) {
        suspendTransaction(database) {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    /**
     * Deletes a user by identifier.
     *
     * @param id The user ID to delete.
     */
    suspend fun delete(id: UInt) {
        suspendTransaction(database) { Users.deleteWhere { Users.id.eq(id) } }
    }

}
