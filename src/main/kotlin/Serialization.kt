@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.adrianobispo

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Configures Ktor's content negotiation to use JSON serialization with the project's
 * preferred defaults, including snake_case field names and tolerant deserialization.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
                namingStrategy = JsonNamingStrategy.SnakeCase
            },
        )
    }
}