@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.adrianobispo.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Creates the shared JSON configuration used by the API.
 *
 * The serializer is configured to:
 * - accept unknown fields for forward compatibility;
 * - omit explicit nulls when encoding;
 * - include default values in payloads;
 * - use `snake_case` field names for request/response models.
 */
fun taskManagerJson(): Json = Json {
    prettyPrint = false
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    namingStrategy = JsonNamingStrategy.SnakeCase
}
