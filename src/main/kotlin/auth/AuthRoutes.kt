package com.adrianobispo.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequestDto>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequestDto>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

