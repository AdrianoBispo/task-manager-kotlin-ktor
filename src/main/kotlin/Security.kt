package com.adrianobispo

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.cors.routing.CORS

/**
 * Configures application security by installing CORS and JWT authentication.
 *
 * CORS settings are read from `ktor.security.cors`, while JWT settings are read
 * from `ktor.security.jwt`.
 */
fun Application.configureSecurity() {
    val securityConfig = this.environment.config.config("ktor.security")
    val jwtConfig = securityConfig.config("jwt")
    val corsConfig = securityConfig.config("cors")

    val jwtSecret = jwtConfig.propertyOrNull("secret")?.getString().orEmpty()
    val jwtIssuer = jwtConfig.propertyOrNull("issuer")?.getString().orEmpty()
    val jwtAudience = jwtConfig.propertyOrNull("audience")?.getString().orEmpty()
    val jwtRealm = jwtConfig.propertyOrNull("realm")?.getString().orEmpty()

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

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}