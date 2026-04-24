package com.adrianobispo

import com.adrianobispo.auth.AuthService
import com.adrianobispo.tasks.ExposedTaskRepository
import com.adrianobispo.tasks.TaskRepository
import com.adrianobispo.tasks.TaskService
import com.adrianobispo.users.ExposedUserRepository
import com.adrianobispo.users.UserRepository
import io.ktor.server.application.*
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
                single { application.jwtSettings() }

                single<UserRepository> { ExposedUserRepository() }
                single<TaskRepository> { ExposedTaskRepository() }
                single { AuthService(get(), get()) }
                single { TaskService(get()) }
            },
        )
    }
}
