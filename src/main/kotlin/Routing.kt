package com.adrianobispo

import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Configures the application's HTTP routes.
 *
 * Exposes a root endpoint for a simple service identifier and a health check
 * endpoint for basic availability monitoring.
 */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("task-manager-backend")
        }

        get("/health") {
            call.respondText("ok")
        }
    }
}