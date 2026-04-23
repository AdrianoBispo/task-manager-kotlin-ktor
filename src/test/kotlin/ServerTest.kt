package com.adrianobispo

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for the application's top-level server routes.
 */
class ServerTest {

    /**
     * Verifies that the root endpoint responds successfully.
     */
    @Test
    fun `test root endpoint`() = testApplication {
        environment {
            config = ApplicationConfig("application.conf")
        }
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

}
