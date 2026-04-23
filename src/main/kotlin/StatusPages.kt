package com.adrianobispo

import com.adrianobispo.shared.ApiErrorEnvelope
import com.adrianobispo.shared.ApiException
import com.adrianobispo.shared.toEnvelope
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

/**
 * Configures the application's status pages plugin to handle uncaught exceptions.
 *
 * Any unhandled [Throwable] is mapped to a consistent JSON error envelope.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, cause.toEnvelope())
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ApiErrorEnvelope(
                    status = HttpStatusCode.BadRequest.value,
                    erro = "bad_request",
                    mensagem = cause.message ?: "Invalid request",
                ),
            )
        }

        exception<NoSuchElementException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ApiErrorEnvelope(
                    status = HttpStatusCode.NotFound.value,
                    erro = "not_found",
                    mensagem = cause.message ?: "Resource not found",
                ),
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ApiErrorEnvelope(
                    status = HttpStatusCode.InternalServerError.value,
                    erro = "internal_server_error",
                    mensagem = cause.message ?: "Internal Server Error",
                ),
            )
        }
    }
}
