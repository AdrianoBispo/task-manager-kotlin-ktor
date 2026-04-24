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
    companion object {
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_LIMIT = 10
    }

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
        val dataVencimento = request.dataVencimento?.let {
            runCatching { Instant.parse(it) }.getOrElse {
                throw ApiException(
                    status = HttpStatusCode.BadRequest,
                    error = "validacao",
                    message = "data_vencimento inválida",
                )
            }
        }
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
        return listByOwner(
            ownerId = ownerId,
            status = null,
            prioridade = null,
            q = null,
            sort = null,
            page = null,
            limit = null,
        )
    }

    fun listByOwner(
        ownerId: UUID,
        status: String?,
        prioridade: String?,
        q: String?,
        sort: String?,
        page: String?,
        limit: String?,
    ): TaskListResponseDto {
        val normalizedQuery = normalizeQuery(status, prioridade, q, sort, page, limit)
        val result = taskRepository.listByOwner(ownerId, normalizedQuery)
        val totalPaginas = if (result.totalItems == 0) 0 else (result.totalItems + normalizedQuery.limit - 1) / normalizedQuery.limit

        return TaskListResponseDto(
            dados = result.items.map { it.toDto() },
            meta = TaskListMetaDto(
                totalItens = result.totalItems,
                totalPaginas = totalPaginas,
                paginaAtual = normalizedQuery.page,
                itensPorPagina = normalizedQuery.limit,
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

    private fun normalizeQuery(
        status: String?,
        prioridade: String?,
        q: String?,
        sort: String?,
        page: String?,
        limit: String?,
    ): TaskListQuery {
        val normalizedStatus = status?.trim()?.takeIf { it.isNotEmpty() }?.let {
            runCatching { TaskStatus.valueOf(it.uppercase()) }.getOrElse {
                throw ApiException(HttpStatusCode.BadRequest, "validacao", "status inválido")
            }
        }

        val normalizedPriority = prioridade?.trim()?.takeIf { it.isNotEmpty() }?.let {
            runCatching { TaskPriority.valueOf(it.uppercase()) }.getOrElse {
                throw ApiException(HttpStatusCode.BadRequest, "validacao", "prioridade inválida")
            }
        }

        val normalizedSort = when (sort?.trim()?.takeIf { it.isNotEmpty() } ?: "-data_criacao") {
            "data_vencimento" -> TaskSort(TaskSortField.DATA_VENCIMENTO, TaskSortDirection.ASC)
            "-data_vencimento" -> TaskSort(TaskSortField.DATA_VENCIMENTO, TaskSortDirection.DESC)
            "data_criacao" -> TaskSort(TaskSortField.DATA_CRIACAO, TaskSortDirection.ASC)
            "-data_criacao" -> TaskSort(TaskSortField.DATA_CRIACAO, TaskSortDirection.DESC)
            else -> throw ApiException(HttpStatusCode.BadRequest, "validacao", "sort inválido")
        }

        val normalizedPage = page?.toIntOrNull() ?: DEFAULT_PAGE
        val normalizedLimit = limit?.toIntOrNull() ?: DEFAULT_LIMIT

        if (normalizedPage < 1) {
            throw ApiException(HttpStatusCode.BadRequest, "validacao", "page deve ser maior ou igual a 1")
        }

        if (normalizedLimit < 1) {
            throw ApiException(HttpStatusCode.BadRequest, "validacao", "limit deve ser maior ou igual a 1")
        }

        return TaskListQuery(
            status = normalizedStatus,
            prioridade = normalizedPriority,
            q = q?.trim()?.ifBlank { null },
            sort = normalizedSort,
            page = normalizedPage,
            limit = normalizedLimit,
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

