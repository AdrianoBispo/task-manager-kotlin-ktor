package com.adrianobispo.contract

import com.adrianobispo.resetDatabaseSchema
import com.adrianobispo.shared.DatabaseFactory
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TaskEditDeleteContractTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `PATCH tasks id returns updated task for partial payload`() = testApplication {
        configureApp()
        val token = registerAndGetToken("edit-contract@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa original","prioridade":"MEDIA"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val response = client.patch("/api/tasks/$taskId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa editada","prioridade":"ALTA"}""")
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Tarefa editada", payload["titulo"]?.jsonPrimitive?.content)
        assertEquals("ALTA", payload["prioridade"]?.jsonPrimitive?.content)
        assertNotNull(payload["data_atualizacao"])
    }

    @Test
    fun `PATCH and DELETE tasks id reject access from another user with error envelope`() = testApplication {
        configureApp()

        val ownerToken = registerAndGetToken("owner-contract@email.com")
        val otherToken = registerAndGetToken("other-contract@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa privada"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val patchResponse = client.patch("/api/tasks/$taskId") {
            bearerAuth(otherToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tentativa indevida"}""")
        }
        val patchPayload = Json.parseToJsonElement(patchResponse.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.Forbidden, patchResponse.status)
        assertEquals(HttpStatusCode.Forbidden.value, patchPayload["status"]?.jsonPrimitive?.content?.toInt())
        assertNotNull(patchPayload["erro"])
        assertNotNull(patchPayload["mensagem"])

        val deleteResponse = client.delete("/api/tasks/$taskId") { bearerAuth(otherToken) }
        val deletePayload = Json.parseToJsonElement(deleteResponse.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.Forbidden, deleteResponse.status)
        assertEquals(HttpStatusCode.Forbidden.value, deletePayload["status"]?.jsonPrimitive?.content?.toInt())
        assertNotNull(deletePayload["erro"])
        assertNotNull(deletePayload["mensagem"])
    }

    @Test
    fun `DELETE tasks id returns 204 for owner`() = testApplication {
        configureApp()
        val token = registerAndGetToken("delete-contract@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Excluir contrato"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val response = client.delete("/api/tasks/$taskId") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NoContent, response.status)
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

