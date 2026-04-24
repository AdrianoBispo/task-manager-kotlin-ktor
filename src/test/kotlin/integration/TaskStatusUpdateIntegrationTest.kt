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
import kotlin.test.assertNotNull

class TaskStatusUpdateIntegrationTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `only task owner can update status and changes are persisted`() = testApplication {
        configureApp()

        val ownerToken = registerAndGetToken("owner-status@email.com")
        val otherToken = registerAndGetToken("other-status@email.com")

        val created = client.post("/api/tasks") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa privada"}""")
        }
        val taskId = Json.parseToJsonElement(created.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val forbidden = client.patch("/api/tasks/$taskId") {
            bearerAuth(otherToken)
            contentType(ContentType.Application.Json)
            setBody("""{"status":"CONCLUIDA"}""")
        }
        assertEquals(HttpStatusCode.Forbidden, forbidden.status)

        val updated = client.patch("/api/tasks/$taskId") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"status":"CONCLUIDA"}""")
        }
        val updatedPayload = Json.parseToJsonElement(updated.bodyAsText()).jsonObject

        assertEquals(HttpStatusCode.OK, updated.status)
        assertEquals("CONCLUIDA", updatedPayload["status"]?.jsonPrimitive?.content)
        assertNotNull(updatedPayload["data_conclusao"]?.jsonPrimitive?.content)

        val ownerList = client.get("/api/tasks") { bearerAuth(ownerToken) }
        val ownerTask = Json.parseToJsonElement(ownerList.bodyAsText()).jsonObject["dados"]
            ?.jsonArray
            ?.first()
            ?.jsonObject

        assertNotNull(ownerTask)
        assertEquals("CONCLUIDA", ownerTask["status"]?.jsonPrimitive?.content)
        assertNotNull(ownerTask["data_conclusao"]?.jsonPrimitive?.content)
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

