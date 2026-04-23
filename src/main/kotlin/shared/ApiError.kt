package com.adrianobispo.shared

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

/**
 * Represents a single validation or domain error detail returned by the API.
 *
 * @property campo Optional field name associated with the error.
 * @property erro Human-readable error description.
 */
@Serializable
data class ApiErrorDetail(
    val campo: String? = null,
    val erro: String,
)

/**
 * Standard envelope used to serialize API errors in responses.
 *
 * @property status HTTP status code value.
 * @property erro Short error identifier or type.
 * @property mensagem Human-readable message describing the failure.
 * @property detalhes Optional list of detailed error entries.
 */
@Serializable
data class ApiErrorEnvelope(
    val status: Int,
    val erro: String,
    val mensagem: String,
    val detalhes: List<ApiErrorDetail>? = null,
)

/**
 * Base exception type for API errors that should be rendered as a structured response.
 *
 * @property status HTTP status to return.
 * @property error Short error identifier or type.
 * @property message Human-readable message describing the failure.
 * @property detalhes Optional list of detailed error entries.
 */
open class ApiException(
    val status: HttpStatusCode,
    val error: String,
    override val message: String,
    val detalhes: List<ApiErrorDetail>? = null,
) : RuntimeException(message)

/**
 * Converts this exception into the API error envelope used in responses.
 *
 * @return A serializable error payload.
 */
fun ApiException.toEnvelope(): ApiErrorEnvelope = ApiErrorEnvelope(
    status = status.value,
    erro = error,
    mensagem = message,
    detalhes = detalhes,
)

