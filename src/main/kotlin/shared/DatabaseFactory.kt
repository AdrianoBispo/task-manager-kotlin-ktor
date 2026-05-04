package com.adrianobispo.shared

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import java.sql.DriverManager

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
            val flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                // Supports first run against existing databases without flyway_schema_history.
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()

            if (shouldResetSchema(url, user, password)) {
                flyway.clean()
            }

            flyway.migrate()
        }

        return Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password,
        )
    }

    private fun shouldResetSchema(url: String, user: String, password: String): Boolean {
        if (!url.contains("localhost") && !url.contains("127.0.0.1")) {
            return false
        }

        return !tableExists(url, user, password, "users") || !tableExists(url, user, password, "tasks")
    }

    private fun tableExists(url: String, user: String, password: String, tableName: String): Boolean {
        DriverManager.getConnection(url, user, password).use { connection ->
            connection.prepareStatement(
                """
                    select exists (
                        select 1
                        from information_schema.tables
                        where table_schema = 'public'
                          and table_name = ?
                    )
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, tableName)
                statement.executeQuery().use { resultSet ->
                    return resultSet.next() && resultSet.getBoolean(1)
                }
            }
        }
    }
}

/**
 * Configures and returns the application's shared [Database] instance.
 */
fun Application.configureDatabase(): Database = DatabaseFactory.init(environment.config)
