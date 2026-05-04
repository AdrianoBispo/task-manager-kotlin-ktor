package com.adrianobispo.unit

import com.adrianobispo.JwtSettings
import com.adrianobispo.auth.AuthService
import com.adrianobispo.auth.LoginRequestDto
import com.adrianobispo.auth.RegisterRequestDto
import com.adrianobispo.shared.ApiException
import com.adrianobispo.users.UserRecord
import com.adrianobispo.users.UserRepository
import io.ktor.http.*
import java.time.Instant
import java.util.*
import kotlin.test.*

class AuthServiceTest {
    @Test
    fun `register hashes password and returns token plus user`() {
        val repository = FakeUserRepository()
        val service = AuthService(repository, testJwtSettings())

        val response = service.register(
            RegisterRequestDto(
                nome = "João",
                email = "joao@email.com",
                senha = "senha123",
            ),
        )

        val storedUser = repository.findByEmail("joao@email.com")
        assertNotNull(storedUser)
        assertNotEquals("senha123", storedUser.senhaHash)
        assertTrue(response.token.isNotBlank())
        assertEquals(storedUser.id.toString(), response.usuario.id)
    }

    @Test
    fun `register with duplicate email throws contract exception`() {
        val repository = FakeUserRepository().apply {
            create("João", "joao@email.com", "hash-existente")
        }
        val service = AuthService(repository, testJwtSettings())

        val error = try {
            service.register(
                RegisterRequestDto(
                    nome = "Maria",
                    email = "joao@email.com",
                    senha = "senha123",
                ),
            )
            null
        } catch (exception: ApiException) {
            exception
        }

        assertNotNull(error)
        assertEquals(HttpStatusCode.UnprocessableEntity, error.status)
    }

    @Test
    fun `login with invalid password throws unauthorized exception`() {
        val repository = FakeUserRepository().apply {
            val registered = AuthService(this, testJwtSettings()).register(
                RegisterRequestDto(
                    nome = "João",
                    email = "joao@email.com",
                    senha = "senha123",
                ),
            )
            assertNotNull(registered.token)
        }
        val service = AuthService(repository, testJwtSettings())

        val error = try {
            service.login(LoginRequestDto(email = "joao@email.com", senha = "errada"))
            null
        } catch (exception: ApiException) {
            exception
        }

        assertNotNull(error)
        assertEquals(HttpStatusCode.Unauthorized, error.status)
    }

    @Test
    fun `login updates last_login timestamp`() {
        val repository = FakeUserRepository()
        val service = AuthService(repository, testJwtSettings())

        service.register(
            RegisterRequestDto(
                nome = "João",
                email = "joao@email.com",
                senha = "senha123",
            ),
        )

        service.login(LoginRequestDto(email = "joao@email.com", senha = "senha123"))

        assertNotNull(repository.lastLoginUpdatedAt)
    }

    private fun testJwtSettings() = JwtSettings(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm",
    )
}

private class FakeUserRepository : UserRepository {
    private val usersByEmail = linkedMapOf<String, UserRecord>()
    var lastLoginUpdatedAt: Instant? = null

    override fun findByEmail(email: String): UserRecord? = usersByEmail[email.trim().lowercase()]

    override fun create(nome: String, email: String, senhaHash: String, now: Instant): UserRecord {
        val user = UserRecord(
            id = UUID.randomUUID(),
            nome = nome,
            email = email.trim().lowercase(),
            senhaHash = senhaHash,
            dataCriacao = now,
            ultimoLogin = null,
        )
        usersByEmail[user.email] = user
        return user
    }

    override fun updateLastLogin(email: String, now: Instant) {
        val existing = usersByEmail[email.trim().lowercase()] ?: return
        usersByEmail[existing.email] = existing.copy(ultimoLogin = now)
        lastLoginUpdatedAt = now
    }
}


