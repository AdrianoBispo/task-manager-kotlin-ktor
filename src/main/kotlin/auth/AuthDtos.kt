package com.adrianobispo.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val nome: String,
    val email: String,
    val senha: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val senha: String,
)

@Serializable
data class AuthUserDto(
    val id: String,
    val nome: String,
    val email: String,
)

@Serializable
data class AuthResponseDto(
    val usuario: AuthUserDto,
    val token: String,
)

