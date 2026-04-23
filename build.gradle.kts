/**
 * Gradle build configuration for the Task Manager backend.
 *
 * This module uses:
 * - Kotlin JVM 21
 * - Ktor for the HTTP server and application modules
 * - kotlinx.serialization for JSON support
 * - Exposed for database access
 * - Flyway for database migrations
 * - Koin for dependency injection
 * - bcrypt for password hashing
 * - MockK and Testcontainers for testing
 */
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.adrianobispo"
version = "1.0.0-SNAPSHOT"

/**
 * Entry point used by the Ktor Gradle plugin to start the Netty server.
 */
application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

/**
 * Ensures the project is compiled against Java 21.
 */
kotlin {
    jvmToolchain(21)
}

dependencies {
    // --- Ktor runtime ---
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.statusPages)

    // --- Database and persistence ---
    implementation(libs.exposed.core)
    implementation(libs.exposed.r2dbc)
    implementation("org.jetbrains.exposed:exposed-dao:1.2.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.2.0")
    implementation("org.jetbrains.exposed:exposed-java-time:1.2.0")

    // --- Migrations and infrastructure ---
    implementation("org.flywaydb:flyway-core:11.8.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.8.2")

    // --- Dependency injection and utilities ---
    implementation(libs.insert.koin.koinKtor)
    implementation(libs.insert.koin.koinLoggerSlf4j)
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation(libs.logback.classic)
    implementation(libs.postgresql)

    // --- Testing ---
    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.5"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

/**
 * Uses JUnit Platform for running tests.
 */
tasks.test {
    useJUnitPlatform()
}
