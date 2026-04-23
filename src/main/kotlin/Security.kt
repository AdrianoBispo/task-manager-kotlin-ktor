package com.adrianobispo

import com.adrianobispo.shared.AuthPrincipal
import com.adrianobispo.shared.toAuthPrincipal
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.cors.routing.CORS
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * JWT settings loaded from application configuration.
 */
data class JwtSettings(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
)

fun Application.jwtSettings(): JwtSettings {
    val jwtConfig = environment.config.config("ktor.security.jwt")
    return JwtSettings(
        secret = jwtConfig.property("secret").getString(),
        issuer = jwtConfig.property("issuer").getString(),
        audience = jwtConfig.property("audience").getString(),
        realm = jwtConfig.property("realm").getString(),
    )
}

fun JwtSettings.verifier() = JWT
    .require(Algorithm.HMAC256(secret))
    .withIssuer(issuer)
    .withAudience(audience)
    .build()

fun JwtSettings.createToken(principal: AuthPrincipal, expiresAt: Instant = Instant.now().plus(24, ChronoUnit.HOURS)): String =
    JWT.create()
        .withSubject(principal.userId.toString())
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("nome", principal.nome)
        .withClaim("email", principal.email)
        .withExpiresAt(Date.from(expiresAt))
        .sign(Algorithm.HMAC256(secret))

/**
 * Configures application security by installing CORS and JWT authentication.
 */
fun Application.configureSecurity() {
    val securityConfig = environment.config.config("ktor.security")
    val corsConfig = securityConfig.config("cors")
    val jwtSettings = jwtSettings()

    val corsHosts = corsConfig.propertyOrNull("allowed-hosts")
        ?.getString()
        .orEmpty()
        .split(',')
        .map(String::trim)
        .filter(String::isNotEmpty)

    val allowCredentialsFlag = corsConfig
        .propertyOrNull("allow-credentials")
        ?.getString()
        ?.toBooleanStrictOrNull()
        ?: false

    install(CORS) {
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowCredentials = allowCredentialsFlag
        if (corsHosts.isEmpty()) {
            anyHost()
        } else {
            corsHosts.forEach { host ->
                val (scheme, normalizedHost) = if (host.startsWith("https://")) {
                    "https" to host.removePrefix("https://")
                } else {
                    "http" to host.removePrefix("http://")
                }
                allowHost(normalizedHost, schemes = listOf(scheme))
            }
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtSettings.realm
            verifier(jwtSettings.verifier())
            validate { credential -> credential.toAuthPrincipal() }
        }
    }
}