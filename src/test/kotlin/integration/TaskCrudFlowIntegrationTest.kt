package com.adrianobispo.integration

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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskCrudFlowIntegrationTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `auth create list update and delete task flow works end to end`() = testApplication {
        configureApp()

        val token = registerAndGetToken("crud-flow-${System.currentTimeMillis()}@email.com")

        val createResponse = client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(
                """{
                    "titulo":"Tarefa integração",
                    "descricao":"fluxo completo",
                    "status":"PENDENTE",
                    "prioridade":"ALTA"
                }""".trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)

        val createdPayload = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val taskId = createdPayload["id"]!!.jsonPrimitive.content
        assertEquals("Tarefa integração", createdPayload["titulo"]!!.jsonPrimitive.content)
        assertEquals("PENDENTE", createdPayload["status"]!!.jsonPrimitive.content)
        assertEquals("ALTA", createdPayload["prioridade"]!!.jsonPrimitive.content)

        val listResponse = client.get("/api/tasks") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, listResponse.status)
        val listPayload = Json.parseToJsonElement(listResponse.bodyAsText()).jsonObject
        val items = listPayload["dados"]!!.jsonArray
        assertEquals(1, items.size)
        assertEquals(taskId, items.single().jsonObject["id"]!!.jsonPrimitive.content)

        val updateResponse = client.patch("/api/tasks/$taskId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(
                """{
                    "titulo":"Tarefa integração atualizada",
                    "status":"EM_ANDAMENTO"
                }""".trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val updatedPayload = Json.parseToJsonElement(updateResponse.bodyAsText()).jsonObject
        assertEquals("Tarefa integração atualizada", updatedPayload["titulo"]!!.jsonPrimitive.content)
        assertEquals("EM_ANDAMENTO", updatedPayload["status"]!!.jsonPrimitive.content)

        val deletedResponse = client.delete("/api/tasks/$taskId") { bearerAuth(token) }
        assertEquals(HttpStatusCode.NoContent, deletedResponse.status)

        val finalListResponse = client.get("/api/tasks") { bearerAuth(token) }
        assertEquals(HttpStatusCode.OK, finalListResponse.status)
        val finalItems = Json.parseToJsonElement(finalListResponse.bodyAsText()).jsonObject["dados"]!!.jsonArray
        assertTrue(finalItems.isEmpty())
    }

    private suspend fun io.ktor.server.testing.ApplicationTestBuilder.registerAndGetToken(email: String): String {
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"nome":"Usuário","email":"$email","senha":"senha123"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return payload["token"]?.jsonPrimitive?.content ?: error("Token não retornado")
    }
}


