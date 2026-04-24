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

class TaskEditDeleteIntegrationTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `only owner can edit and delete task and persistence reflects changes`() = testApplication {
        configureApp()

        val ownerToken = registerAndGetToken("owner-edit-delete@email.com")
        val otherToken = registerAndGetToken("other-edit-delete@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa privada","descricao":"inicial"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val forbiddenPatch = client.patch("/api/tasks/$taskId") {
            bearerAuth(otherToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"alteração indevida"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, forbiddenPatch.status)

        val ownerPatch = client.patch("/api/tasks/$taskId") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa editada","status":"EM_ANDAMENTO"}""")
        }
        val patchPayload = Json.parseToJsonElement(ownerPatch.bodyAsText()).jsonObject
        assertEquals(HttpStatusCode.OK, ownerPatch.status)
        assertEquals("Tarefa editada", patchPayload["titulo"]?.jsonPrimitive?.content)
        assertEquals("EM_ANDAMENTO", patchPayload["status"]?.jsonPrimitive?.content)

        val forbiddenDelete = client.delete("/api/tasks/$taskId") { bearerAuth(otherToken) }
        assertEquals(HttpStatusCode.Forbidden, forbiddenDelete.status)

        val ownerDelete = client.delete("/api/tasks/$taskId") { bearerAuth(ownerToken) }
        assertEquals(HttpStatusCode.NoContent, ownerDelete.status)

        val ownerList = client.get("/api/tasks") { bearerAuth(ownerToken) }
        val ownerItems = Json.parseToJsonElement(ownerList.bodyAsText()).jsonObject["dados"]!!.jsonArray
        assertEquals(0, ownerItems.size)
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

