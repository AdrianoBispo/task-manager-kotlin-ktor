package com.adrianobispo.tasks

import com.adrianobispo.shared.ApiException
import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID

class TaskService(
    private val taskRepository: TaskRepository,
) {
    fun create(ownerId: UUID, request: CreateTaskRequestDto): TaskDto {
        val titulo = request.titulo.trim()
        if (titulo.length !in 3..100) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                error = "validacao",
                message = "Título deve ter entre 3 e 100 caracteres",
            )
        }

        val now = Instant.now()
        val status = request.status ?: TaskStatus.PENDENTE
        val prioridade = request.prioridade ?: TaskPriority.MEDIA
        val dataVencimento = request.dataVencimento?.let(Instant::parse)
        val dataConclusao = if (status == TaskStatus.CONCLUIDA) now else null

        return taskRepository.create(
            ownerId = ownerId,
            titulo = titulo,
            descricao = request.descricao?.trim()?.ifBlank { null },
            status = status,
            prioridade = prioridade,
            dataVencimento = dataVencimento,
            dataCriacao = now,
            dataAtualizacao = now,
            dataConclusao = dataConclusao,
        ).toDto()
    }

    fun listByOwner(ownerId: UUID): TaskListResponseDto {
        val tasks = taskRepository.listByOwner(ownerId).map { it.toDto() }
        val totalItens = tasks.size

        return TaskListResponseDto(
            dados = tasks,
            meta = TaskListMetaDto(
                totalItens = totalItens,
                totalPaginas = if (totalItens == 0) 0 else 1,
                paginaAtual = 1,
                itensPorPagina = if (totalItens == 0) 10 else totalItens,
            ),
        )
    }

    private fun TaskRecord.toDto(): TaskDto = TaskDto(
        id = id.toString(),
        titulo = titulo,
        descricao = descricao,
        status = status,
        prioridade = prioridade,
        dataVencimento = dataVencimento?.toString(),
        dataCriacao = dataCriacao.toString(),
        dataAtualizacao = dataAtualizacao.toString(),
        dataConclusao = dataConclusao?.toString(),
    )
}

