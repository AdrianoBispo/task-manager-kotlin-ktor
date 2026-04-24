package com.adrianobispo.shared

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

/**
 * Status values for a task lifecycle.
 */
enum class TaskStatus {
    PENDENTE,
    EM_ANDAMENTO,
    CONCLUIDA,
}

/**
 * Priority levels used to classify tasks.
 */
enum class TaskPriority {
    BAIXA,
    MEDIA,
    ALTA,
}

/**
 * Exposed table mapping for system users.
 */
object UsersTable : UUIDTable("users") {
    val nome = varchar("nome", 120)
    val email = varchar("email", 255).uniqueIndex()
    val senhaHash = varchar("senha_hash", 255)
    val dataCriacao = timestamp("data_criacao")
    val ultimoLogin = timestamp("ultimo_login").nullable()
}

/**
 * Exposed table mapping for user tasks.
 */
object TasksTable : UUIDTable("tasks") {
    val idUsuario = reference("id_usuario", UsersTable, onDelete = ReferenceOption.CASCADE)
    val titulo = varchar("titulo", 100)
    val descricao = text("descricao").nullable()
    val status = enumerationByName("status", 20, TaskStatus::class).default(TaskStatus.PENDENTE)
    val prioridade = enumerationByName("prioridade", 20, TaskPriority::class).default(TaskPriority.MEDIA)
    val dataVencimento = timestamp("data_vencimento").nullable()
    val dataCriacao = timestamp("data_criacao")
    val dataAtualizacao = timestamp("data_atualizacao")
    val dataConclusao = timestamp("data_conclusao").nullable()
}
