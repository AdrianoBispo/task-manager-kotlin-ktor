package com.adrianobispo.users

import com.adrianobispo.shared.UsersTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.*

data class UserRecord(
    val id: UUID,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val dataCriacao: Instant,
    val ultimoLogin: Instant?,
)

interface UserRepository {
    fun findByEmail(email: String): UserRecord?
    fun create(nome: String, email: String, senhaHash: String, now: Instant = Instant.now()): UserRecord
    fun updateLastLogin(email: String, now: Instant = Instant.now())
}

class ExposedUserRepository : UserRepository {
    override fun findByEmail(email: String): UserRecord? = transaction {
        val normalizedEmail = email.trim().lowercase()
        UsersTable
            .selectAll()
            .where { UsersTable.email eq normalizedEmail }
            .singleOrNull()
            ?.toUserRecord()
    }

    override fun create(nome: String, email: String, senhaHash: String, now: Instant): UserRecord {
        val normalizedEmail = email.trim().lowercase()
        val userId = UUID.randomUUID()

        transaction {
            UsersTable.insert {
                it[id] = userId
                it[this.nome] = nome
                it[this.email] = normalizedEmail
                it[this.senhaHash] = senhaHash
                it[dataCriacao] = now
                it[ultimoLogin] = null
            }
        }

        return UserRecord(
            id = userId,
            nome = nome,
            email = normalizedEmail,
            senhaHash = senhaHash,
            dataCriacao = now,
            ultimoLogin = null,
        )
    }

    override fun updateLastLogin(email: String, now: Instant) {
        val normalizedEmail = email.trim().lowercase()
        transaction {
            UsersTable.update({ UsersTable.email eq normalizedEmail }) {
                it[ultimoLogin] = now
            }
        }
    }

    private fun ResultRow.toUserRecord(): UserRecord = UserRecord(
        id = this[UsersTable.id].value,
        nome = this[UsersTable.nome],
        email = this[UsersTable.email],
        senhaHash = this[UsersTable.senhaHash],
        dataCriacao = this[UsersTable.dataCriacao],
        ultimoLogin = this[UsersTable.ultimoLogin],
    )
}

