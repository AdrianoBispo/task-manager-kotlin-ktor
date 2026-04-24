package com.adrianobispo.contract

import com.adrianobispo.resetDatabaseSchema
import com.adrianobispo.shared.DatabaseFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TaskCreateListContractTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `POST tasks returns 201 with default status and prioridade when omitted`() = testApplication {
        configureApp()
        val token = registerAndGetToken("maria@email.com")

        val response = client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Estudar Ktor"}""")
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("PENDENTE", payload["status"]?.jsonPrimitive?.content)
        assertEquals("MEDIA", payload["prioridade"]?.jsonPrimitive?.content)
        assertEquals("Estudar Ktor", payload["titulo"]?.jsonPrimitive?.content)
        assertNotNull(payload["id"])
    }

    @Test
    fun `GET tasks returns authenticated owner list envelope`() = testApplication {
        configureApp()
        val token = registerAndGetToken("joao@email.com")

        client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Minha tarefa"}""")
        }

        val response = client.get("/api/tasks") {
            bearerAuth(token)
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(payload["meta"])
        val dados = payload["dados"]?.jsonArray
        assertNotNull(dados)
        assertEquals(1, dados.size)
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


