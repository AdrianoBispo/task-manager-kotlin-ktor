package com.adrianobispo.tasks

import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequestDto(
    val titulo: String,
    val descricao: String? = null,
    val status: TaskStatus? = null,
    val prioridade: TaskPriority? = null,
    val dataVencimento: String? = null,
)

@Serializable
data class UpdateTaskStatusRequestDto(
    val status: TaskStatus,
)

@Serializable
data class UpdateTaskRequestDto(
    val titulo: String? = null,
    val descricao: String? = null,
    val status: TaskStatus? = null,
    val prioridade: TaskPriority? = null,
    val dataVencimento: String? = null,
)

@Serializable
data class TaskDto(
    val id: String,
    val titulo: String,
    val descricao: String? = null,
    val status: TaskStatus,
    val prioridade: TaskPriority,
    val dataVencimento: String? = null,
    val dataCriacao: String,
    val dataAtualizacao: String,
    val dataConclusao: String? = null,
)

@Serializable
data class TaskListMetaDto(
    val totalItens: Int,
    val totalPaginas: Int,
    val paginaAtual: Int,
    val itensPorPagina: Int,
)

@Serializable
data class TaskListResponseDto(
    val dados: List<TaskDto>,
    val meta: TaskListMetaDto,
)

