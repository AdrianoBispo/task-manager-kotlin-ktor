package com.adrianobispo.integration

import com.adrianobispo.resetDatabaseSchema
import com.adrianobispo.shared.DatabaseFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskVisibilityTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `created tasks are visible only to the owner`() = testApplication {
        configureApp()

        val tokenUserA = registerAndGetToken("alice@email.com")
        val tokenUserB = registerAndGetToken("bob@email.com")

        client.post("/api/tasks") {
            bearerAuth(tokenUserA)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa da Alice"}""")
        }

        client.post("/api/tasks") {
            bearerAuth(tokenUserB)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa do Bob"}""")
        }

        val responseA = client.get("/api/tasks") { bearerAuth(tokenUserA) }
        val responseB = client.get("/api/tasks") { bearerAuth(tokenUserB) }

        assertEquals(HttpStatusCode.OK, responseA.status)
        assertEquals(HttpStatusCode.OK, responseB.status)

        val dadosA = Json.parseToJsonElement(responseA.bodyAsText()).jsonObject["dados"]!!.jsonArray
        val dadosB = Json.parseToJsonElement(responseB.bodyAsText()).jsonObject["dados"]!!.jsonArray

        assertEquals(1, dadosA.size)
        assertEquals(1, dadosB.size)
        assertTrue(dadosA.any { it.jsonObject["titulo"]?.jsonPrimitive?.content == "Tarefa da Alice" })
        assertTrue(dadosB.any { it.jsonObject["titulo"]?.jsonPrimitive?.content == "Tarefa do Bob" })
    }

    private suspend fun io.ktor.server.testing.ApplicationTestBuilder.registerAndGetToken(email: String): String {
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Usuário","email":"$email","senha":"senha123"}""")
        }
        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return payload["token"]?.jsonPrimitive?.content ?: error("Token não retornado")
    }
}


