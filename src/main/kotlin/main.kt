package com.adrianobispo

import com.adrianobispo.shared.configureDatabase
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

/**
 * Starts the Ktor Netty engine using command-line arguments.
 *
 * @param args command-line arguments forwarded to the engine.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Configures the application modules and plugins for this service.
 *
 * The setup includes serialization, security, status pages, database,
 * dependency injection, and routing.
 */
fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureDatabase()
    configureKoin()
    configureRouting()
}
