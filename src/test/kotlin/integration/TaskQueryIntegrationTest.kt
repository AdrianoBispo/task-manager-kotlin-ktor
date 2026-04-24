package com.adrianobispo.integration

import com.adrianobispo.resetDatabaseSchema
import com.adrianobispo.shared.DatabaseFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

class TaskQueryIntegrationTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `query behavior combines search filters sorting pagination and owner isolation`() = testApplication {
        configureApp()

        val ownerToken = registerAndGetToken("query-owner@email.com")
        val otherToken = registerAndGetToken("query-other@email.com")

        client.post("/api/tasks") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Fix urgente 1","descricao":"Backend","status":"PENDENTE","prioridade":"ALTA","data_vencimento":"2030-01-03T00:00:00Z"}""")
        }
        client.post("/api/tasks") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Fix urgente 2","descricao":"Frontend","status":"PENDENTE","prioridade":"ALTA","data_vencimento":"2030-01-01T00:00:00Z"}""")
        }
        client.post("/api/tasks") {
            bearerAuth(ownerToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Tarefa comum","status":"PENDENTE","prioridade":"MEDIA"}""")
        }
        client.post("/api/tasks") {
            bearerAuth(otherToken)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Fix urgente externo","status":"PENDENTE","prioridade":"ALTA","data_vencimento":"2030-01-02T00:00:00Z"}""")
        }

        val response = client.get("/api/tasks?status=PENDENTE&prioridade=ALTA&q=urgente&sort=data_vencimento&page=2&limit=1") {
            bearerAuth(ownerToken)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val dados = payload["dados"]!!.jsonArray
        val meta = payload["meta"]!!.jsonObject

        assertEquals(1, dados.size)
        val taskTitle = dados.first().jsonObject["titulo"]!!.jsonPrimitive.content
        assertEquals("Fix urgente 1", taskTitle)
        assertTrue(taskTitle != "Fix urgente externo")

        assertEquals(2, meta["total_itens"]!!.jsonPrimitive.content.toInt())
        assertEquals(2, meta["total_paginas"]!!.jsonPrimitive.content.toInt())
        assertEquals(2, meta["pagina_atual"]!!.jsonPrimitive.content.toInt())
        assertEquals(1, meta["itens_por_pagina"]!!.jsonPrimitive.content.toInt())
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



