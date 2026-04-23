package com.adrianobispo

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText

/**
 * Configures the application's status pages plugin to handle uncaught exceptions.
 *
 * Any unhandled [Throwable] is mapped to a generic 500 response to avoid leaking
 * implementation details to clients.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, _ ->
            call.respondText(
                text = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
            )
        }
    }
}
