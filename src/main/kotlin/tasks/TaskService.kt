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

    fun updateStatus(ownerId: UUID, taskId: UUID, request: UpdateTaskStatusRequestDto): TaskDto {
        return updateTask(ownerId, taskId, UpdateTaskRequestDto(status = request.status))
    }

    fun updateTask(ownerId: UUID, taskId: UUID, request: UpdateTaskRequestDto): TaskDto {
        val existingTask = taskRepository.findById(taskId)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                error = "tarefa_nao_encontrada",
                message = "Tarefa não encontrada",
            )

        if (existingTask.idUsuario != ownerId) {
            throw ApiException(
                status = HttpStatusCode.Forbidden,
                error = "acesso_negado",
                message = "Você não tem permissão para alterar esta tarefa",
            )
        }

        if (
            request.titulo == null &&
            request.descricao == null &&
            request.status == null &&
            request.prioridade == null &&
            request.dataVencimento == null
        ) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                error = "validacao",
                message = "Pelo menos um campo deve ser informado para atualização",
            )
        }

        val updatedTitulo = request.titulo?.trim()?.also {
            if (it.length !in 3..100) {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    error = "validacao",
                    message = "Título deve ter entre 3 e 100 caracteres",
                )
            }
        } ?: existingTask.titulo

        val requestedStatus = request.status ?: existingTask.status

        if (requestedStatus != existingTask.status && !isStatusTransitionAllowed(existingTask.status, requestedStatus)) {
            throw ApiException(
                status = HttpStatusCode.BadRequest,
                error = "transicao_status_invalida",
                message = "Transição de status não permitida",
            )
        }

        val dataVencimento = request.dataVencimento?.let {
            runCatching { Instant.parse(it) }.getOrElse {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    error = "validacao",
                    message = "data_vencimento inválida",
                )
            }
        }

        val now = Instant.now()
        val dataConclusao = when {
            requestedStatus == TaskStatus.CONCLUIDA && existingTask.status != TaskStatus.CONCLUIDA -> now
            requestedStatus != TaskStatus.CONCLUIDA -> null
            else -> existingTask.dataConclusao
        }

        return taskRepository.updateByIdAndOwner(
            taskId = taskId,
            ownerId = ownerId,
            titulo = updatedTitulo,
            descricao = request.descricao?.trim()?.ifBlank { null } ?: existingTask.descricao,
            status = requestedStatus,
            prioridade = request.prioridade ?: existingTask.prioridade,
            dataVencimento = dataVencimento ?: existingTask.dataVencimento,
            dataAtualizacao = now,
            dataConclusao = dataConclusao,
        )?.toDto() ?: throw ApiException(
            status = HttpStatusCode.NotFound,
            error = "tarefa_nao_encontrada",
            message = "Tarefa não encontrada",
        )
    }

    fun delete(ownerId: UUID, taskId: UUID) {
        val existingTask = taskRepository.findById(taskId)
            ?: throw ApiException(
                status = HttpStatusCode.NotFound,
                error = "tarefa_nao_encontrada",
                message = "Tarefa não encontrada",
            )

        if (existingTask.idUsuario != ownerId) {
            throw ApiException(
                status = HttpStatusCode.Forbidden,
                error = "acesso_negado",
                message = "Você não tem permissão para excluir esta tarefa",
            )
        }

        if (!taskRepository.deleteByIdAndOwner(taskId, ownerId)) {
            throw ApiException(
                status = HttpStatusCode.NotFound,
                error = "tarefa_nao_encontrada",
                message = "Tarefa não encontrada",
            )
        }
    }

    private fun isStatusTransitionAllowed(from: TaskStatus, to: TaskStatus): Boolean = when (from) {
        TaskStatus.PENDENTE -> to == TaskStatus.EM_ANDAMENTO || to == TaskStatus.CONCLUIDA
        TaskStatus.EM_ANDAMENTO -> to == TaskStatus.PENDENTE || to == TaskStatus.CONCLUIDA
        TaskStatus.CONCLUIDA -> to == TaskStatus.EM_ANDAMENTO
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

