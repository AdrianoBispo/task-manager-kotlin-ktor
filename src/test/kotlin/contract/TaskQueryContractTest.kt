package com.adrianobispo.contract

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

class TaskQueryContractTest {
    private fun io.ktor.server.testing.ApplicationTestBuilder.configureApp() {
        val config = ApplicationConfig("application.conf")
        environment { this.config = config }
        DatabaseFactory.init(config)
        resetDatabaseSchema()
    }

    @Test
    fun `GET tasks supports filters search sort and pagination with valid meta`() = testApplication {
        configureApp()
        val token = registerAndGetToken("query-contract@email.com")

        client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Alpha urgente","descricao":"bug","status":"PENDENTE","prioridade":"ALTA","data_vencimento":"2030-01-02T00:00:00Z"}""")
        }

        client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Beta urgente","descricao":"feature","status":"PENDENTE","prioridade":"ALTA","data_vencimento":"2030-01-01T00:00:00Z"}""")
        }

        client.post("/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"titulo":"Gamma","status":"EM_ANDAMENTO","prioridade":"MEDIA"}""")
        }

        val response = client.get("/api/tasks?status=PENDENTE&prioridade=ALTA&q=urgente&sort=data_vencimento&page=1&limit=1") {
            bearerAuth(token)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val dados = payload["dados"]!!.jsonArray
        val meta = payload["meta"]!!.jsonObject

        assertEquals(1, dados.size)
        assertEquals("Beta urgente", dados.first().jsonObject["titulo"]!!.jsonPrimitive.content)
        assertEquals(2, meta["total_itens"]!!.jsonPrimitive.content.toInt())
        assertEquals(2, meta["total_paginas"]!!.jsonPrimitive.content.toInt())
        assertEquals(1, meta["pagina_atual"]!!.jsonPrimitive.content.toInt())
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



