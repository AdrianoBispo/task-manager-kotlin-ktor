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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskStatusUpdateContractTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `PATCH tasks accepts valid status transitions and updates data_conclusao`() = testApplication {
        configureApp()
        val token = registerAndGetToken("status-ok@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa de status"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val toConcluida = client.patch("/api/tasks/$taskId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"status":"CONCLUIDA"}""")
        }
        val conclPayload = Json.parseToJsonElement(toConcluida.bodyAsText()).jsonObject

        assertEquals(HttpStatusCode.OK, toConcluida.status)
        assertEquals("CONCLUIDA", conclPayload["status"]?.jsonPrimitive?.content)
        assertNotNull(conclPayload["data_conclusao"]?.jsonPrimitive?.content)

        val toAndamento = client.patch("/api/tasks/$taskId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"status":"EM_ANDAMENTO"}""")
        }
        val andamentoPayload = Json.parseToJsonElement(toAndamento.bodyAsText()).jsonObject

        assertEquals(HttpStatusCode.OK, toAndamento.status)
        assertEquals("EM_ANDAMENTO", andamentoPayload["status"]?.jsonPrimitive?.content)
        assertNull(andamentoPayload["data_conclusao"])
    }

    @Test
    fun `PATCH tasks rejects disallowed status transitions`() = testApplication {
        configureApp()
        val token = registerAndGetToken("status-fail@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa concluída","status":"CONCLUIDA"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val response = client.patch("/api/tasks/$taskId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"status":"PENDENTE"}""")
        }

        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(payload["mensagem"]?.jsonPrimitive?.content?.contains("Transição de status") == true)
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

