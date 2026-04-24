package com.adrianobispo

import com.adrianobispo.auth.AuthService
import com.adrianobispo.auth.authRoutes
import com.adrianobispo.tasks.TaskService
import com.adrianobispo.tasks.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Configures the application's HTTP routes.
 *
 * Exposes a root endpoint for a simple service identifier and a health check
 * endpoint for basic availability monitoring.
 */
fun Application.configureRouting() {
    val authService by inject<AuthService>()
    val taskService by inject<TaskService>()

    routing {
        get("/") {
            call.respondText("task-manager-backend")
        }

        get("/health") {
            call.respondText("ok")
        }

        authRoutes(authService)
        taskRoutes(taskService)
    }
}