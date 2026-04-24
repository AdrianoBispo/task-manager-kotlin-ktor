package com.adrianobispo.tasks

import com.adrianobispo.shared.AuthPrincipal
import com.adrianobispo.shared.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.taskRoutes(taskService: TaskService) {
    authenticate("auth-jwt") {
        route("/api/tasks") {
            post {
                val principal = call.requireAuthPrincipal()
                val request = call.receive<CreateTaskRequestDto>()
                val response = taskService.create(principal.userId, request)
                call.respond(HttpStatusCode.Created, response)
            }

            get {
                val principal = call.requireAuthPrincipal()
                val response = taskService.listByOwner(
                    ownerId = principal.userId,
                    status = call.request.queryParameters["status"],
                    prioridade = call.request.queryParameters["prioridade"],
                    q = call.request.queryParameters["q"],
                    sort = call.request.queryParameters["sort"],
                    page = call.request.queryParameters["page"],
                    limit = call.request.queryParameters["limit"],
                )
                call.respond(HttpStatusCode.OK, response)
            }

            patch("/{id}") {
                val principal = call.requireAuthPrincipal()
                val taskId = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                } ?: throw IllegalArgumentException("ID da tarefa inválido")

                val request = call.receive<UpdateTaskRequestDto>()
                val response = taskService.updateTask(principal.userId, taskId, request)
                call.respond(HttpStatusCode.OK, response)
            }

            delete("/{id}") {
                val principal = call.requireAuthPrincipal()
                val taskId = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                } ?: throw IllegalArgumentException("ID da tarefa inválido")

                taskService.delete(principal.userId, taskId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.requireAuthPrincipal(): AuthPrincipal {
    return principal<AuthPrincipal>() ?: throw ApiException(
        status = HttpStatusCode.Unauthorized,
        error = "nao_autenticado",
        message = "Usuário não autenticado",
    )
}

