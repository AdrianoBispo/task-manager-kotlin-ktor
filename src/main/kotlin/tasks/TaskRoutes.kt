package com.adrianobispo.tasks

import com.adrianobispo.shared.AuthPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.taskRoutes(taskService: TaskService) {
    authenticate("auth-jwt") {
        route("/api/tasks") {
            post {
                val principal = call.principal<AuthPrincipal>() ?: error("Usuário autenticado não disponível")
                val request = call.receive<CreateTaskRequestDto>()
                val response = taskService.create(principal.userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            get {
                val principal = call.principal<AuthPrincipal>() ?: error("Usuário autenticado não disponível")
                val response = taskService.listByOwner(principal.userId)
                call.respond(HttpStatusCode.OK, response)
            }

            patch("/{id}") {
                val principal = call.principal<AuthPrincipal>() ?: error("Usuário autenticado não disponível")
                val taskId = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                } ?: throw IllegalArgumentException("ID da tarefa inválido")

                val request = call.receive<UpdateTaskStatusRequestDto>()
                val response = taskService.updateStatus(principal.userId, taskId, request)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}

