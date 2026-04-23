package com.adrianobispo.shared

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * Creates and caches a singleton [Database] instance for the application lifecycle.
 */
object DatabaseFactory {
    @Volatile
    private var database: Database? = null

    /**
     * Initializes the shared [Database] connection once and returns it.
     *
     * Uses double-checked locking to avoid creating multiple instances
     * when accessed concurrently.
     */
    fun init(config: ApplicationConfig): Database {
        return database ?: synchronized(this) {
            database ?: createDatabase(config).also { database = it }
        }
    }

    /**
     * Builds a [Database] connection from application config and runs migrations
     * for non-H2 databases.
     */
    private fun createDatabase(config: ApplicationConfig): Database {
        val url = config.property("ktor.postgres.url").getString()
        val user = config.property("ktor.postgres.user").getString()
        val password = config.property("ktor.postgres.password").getString()
        val driver = if (url.startsWith("jdbc:h2:")) "org.h2.Driver" else "org.postgresql.Driver"

        if (!url.startsWith("jdbc:h2:")) {
            Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load()
                .migrate()
        }

        return Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password,
        )
    }
}

/**
 * Configures and returns the application's shared [Database] instance.
 */
fun Application.configureDatabase(): Database = DatabaseFactory.init(environment.config)
