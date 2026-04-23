package com.adrianobispo.shared

import io.ktor.server.auth.Principal
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.UUID

/**
 * Represents the authenticated user extracted from a JWT token.
 *
 * @property userId Unique user identifier from the token subject.
 * @property nome Optional display name claim.
 * @property email Optional email claim.
 */
data class AuthPrincipal(
    val userId: UUID,
    val nome: String? = null,
    val email: String? = null,
) : Principal

/**
 * Converts a [JWTPrincipal] into an [AuthPrincipal].
 *
 * Returns `null` when the JWT subject is missing or cannot be parsed as a UUID.
 */
fun JWTPrincipal.toAuthPrincipal(): AuthPrincipal? {
    val userId = subject?.let(UUID::fromString) ?: return null
    return AuthPrincipal(
        userId = userId,
        nome = getClaim("nome", String::class),
        email = getClaim("email", String::class),
    )
}

/**
 * Converts a [JWTCredential] into an [AuthPrincipal].
 *
 * Returns `null` when the token subject is missing or cannot be parsed as a UUID.
 */
fun JWTCredential.toAuthPrincipal(): AuthPrincipal? {
    val userId = payload.subject?.let(UUID::fromString) ?: return null
    return AuthPrincipal(
        userId = userId,
        nome = payload.getClaim("nome").asString(),
        email = payload.getClaim("email").asString(),
    )
}

/**
 * Casts a generic authenticated [Principal] to [AuthPrincipal].
 *
 * @throws IllegalStateException if the principal is not an [AuthPrincipal].
 */
fun Principal.requireAuthPrincipal(): AuthPrincipal = this as? AuthPrincipal
    ?: error("Authenticated principal is not available")
