package com.adrianobispo.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.adrianobispo.JwtSettings
import com.adrianobispo.createToken
import com.adrianobispo.shared.ApiException
import com.adrianobispo.shared.AuthPrincipal
import com.adrianobispo.users.UserRecord
import com.adrianobispo.users.UserRepository
import io.ktor.http.*
import java.time.Instant

class AuthService(
    private val userRepository: UserRepository,
    private val jwtSettings: JwtSettings,
) {
    fun register(request: RegisterRequestDto): AuthResponseDto {
        validateRegisterRequest(request)

        val normalizedEmail = request.email.trim().lowercase()
        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw ApiException(
                status = HttpStatusCode.UnprocessableEntity,
                error = "email_duplicado",
                message = "E-mail já cadastrado",
            )
        }

        val passwordHash = BCrypt.withDefaults().hashToString(12, request.senha.toCharArray())
        val createdUser = userRepository.create(
            nome = request.nome.trim(),
            email = normalizedEmail,
            senhaHash = passwordHash,
        )

        return buildAuthResponse(createdUser)
    }

    fun login(request: LoginRequestDto): AuthResponseDto {
        val normalizedEmail = request.email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throwInvalidCredentials()

        val isValidPassword = BCrypt.verifyer()
            .verify(request.senha.toCharArray(), user.senhaHash)
            .verified

        if (!isValidPassword) {
            throwInvalidCredentials()
        }

        userRepository.updateLastLogin(normalizedEmail, Instant.now())

        return buildAuthResponse(user)
    }

    private fun validateRegisterRequest(request: RegisterRequestDto) {
        if (request.nome.trim().length !in 3..120) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                error = "validacao",
                message = "Nome deve ter entre 3 e 120 caracteres",
            )
        }

        if (!EMAIL_REGEX.matches(request.email.trim())) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                error = "validacao",
                message = "E-mail inválido",
            )
        }

        if (request.senha.length < 6) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                error = "validacao",
                message = "Senha deve ter ao menos 6 caracteres",
            )
        }
    }

    private fun throwInvalidCredentials(): Nothing {
        throw ApiException(
            status = HttpStatusCode.Unauthorized,
            error = "credenciais_invalidas",
            message = "E-mail ou senha inválidos",
        )
    }

    private fun buildAuthResponse(user: UserRecord): AuthResponseDto = AuthResponseDto(
        usuario = AuthUserDto(
            id = user.id.toString(),
            nome = user.nome,
            email = user.email,
        ),
        token = jwtSettings.createToken(
            AuthPrincipal(
                userId = user.id,
                nome = user.nome,
                email = user.email,
            ),
        ),
    )

    private companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}

