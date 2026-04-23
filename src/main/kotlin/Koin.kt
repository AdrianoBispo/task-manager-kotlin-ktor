package com.adrianobispo

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Configures Koin for the Ktor application.
 *
 * Registers the [Application] instance and its configuration in the dependency
 * graph so other components can inject runtime app context when needed.
 */
fun Application.configureKoin() {
    val application = this

    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { application }
                single { application.environment.config }
            },
        )
    }
}
