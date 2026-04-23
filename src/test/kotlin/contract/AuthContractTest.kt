package com.adrianobispo.contract

import com.adrianobispo.resetDatabaseSchema
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Contract tests for the authentication API endpoints.
 *
 * Tests verify the behavior of register and login endpoints including:
 * - Successful registration with valid credentials
 * - Successful login with valid credentials
 * - Error handling for duplicate email registration
 * - Error handling for invalid login credentials
 */
class AuthContractTest {
    /**
     * Configures the test application with the application configuration and resets the database schema.
     *
     * This is an extension function on [io.ktor.server.testing.ApplicationTestBuilder] that:
     * - Loads the application.conf configuration file
     * - Resets the database schema to a clean state for each test
     */
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        environment {
            config = ApplicationConfig("application.conf")
        }
        resetDatabaseSchema()
    }

    /**
     * Contract test: POST /api/auth/register returns 201 Created with user object and JWT token.
     *
     * Verifies that a successful registration request returns:
     * - HTTP status 201 (Created)
     * - A 'usuario' object in the response payload
     * - A 'token' (JWT) in the response payload
     */
    @Test
    fun `POST register returns 201 with usuario and token`() = testApplication {
        configureApp()

        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Maria","email":"maria@email.com","senha":"senha123"}""")
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.Created, response.status)
        assertNotNull(payload["usuario"])
        assertNotNull(payload["token"])
    }

    /**
     * Contract test: POST /api/auth/login returns 200 OK with user object and JWT token for valid credentials.
     *
     * Verifies that a successful login request with correct email and password returns:
     * - HTTP status 200 (OK)
     * - A 'usuario' object in the response payload
     * - A 'token' (JWT) in the response payload
     */
    @Test
    fun `POST login returns 200 for valid credentials`() = testApplication {
        configureApp()

        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Maria","email":"maria@email.com","senha":"senha123"}""")
        }

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"maria@email.com","senha":"senha123"}""")
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(payload["usuario"])
        assertNotNull(payload["token"])
    }

    @Test
    fun `POST register with duplicate email returns contract error envelope`() = testApplication {
        configureApp()

        val requestBody = """{"nome":"Maria","email":"maria@email.com","senha":"senha123"}"""

        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        assertEquals(HttpStatusCode.UnprocessableEntity.value, payload["status"]?.jsonPrimitive?.content?.toInt())
        assertNotNull(payload["erro"])
        assertNotNull(payload["mensagem"])
    }

    @Test
    fun `POST login with invalid credentials returns contract error envelope`() = testApplication {
        configureApp()

        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Maria","email":"maria@email.com","senha":"senha123"}""")
        }

        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"maria@email.com","senha":"senha_errada"}""")
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(HttpStatusCode.Unauthorized.value, payload["status"]?.jsonPrimitive?.content?.toInt())
        assertNotNull(payload["erro"])
        assertNotNull(payload["mensagem"])
    }
}

