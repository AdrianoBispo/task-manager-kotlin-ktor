package com.adrianobispo.tasks

import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import com.adrianobispo.shared.TasksTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant
import java.util.UUID

data class TaskRecord(
    val id: UUID,
    val idUsuario: UUID,
    val titulo: String,
    val descricao: String?,
    val status: TaskStatus,
    val prioridade: TaskPriority,
    val dataVencimento: Instant?,
    val dataCriacao: Instant,
    val dataAtualizacao: Instant,
    val dataConclusao: Instant?,
)

interface TaskRepository {
    fun create(
        ownerId: UUID,
        titulo: String,
        descricao: String?,
        status: TaskStatus,
        prioridade: TaskPriority,
        dataVencimento: Instant?,
        dataCriacao: Instant,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord

    fun listByOwner(ownerId: UUID): List<TaskRecord>

    fun findById(taskId: UUID): TaskRecord?

    fun updateStatusByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        status: TaskStatus,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord?
}

class ExposedTaskRepository : TaskRepository {
    override fun create(
        ownerId: UUID,
        titulo: String,
        descricao: String?,
        status: TaskStatus,
        prioridade: TaskPriority,
        dataVencimento: Instant?,
        dataCriacao: Instant,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord {
        val taskId = UUID.randomUUID()

        transaction {
            TasksTable.insert {
                it[id] = taskId
                it[idUsuario] = ownerId
                it[this.titulo] = titulo
                it[this.descricao] = descricao
                it[this.status] = status
                it[this.prioridade] = prioridade
                it[this.dataVencimento] = dataVencimento
                it[this.dataCriacao] = dataCriacao
                it[this.dataAtualizacao] = dataAtualizacao
                it[this.dataConclusao] = dataConclusao
            }
        }

        return TaskRecord(
            id = taskId,
            idUsuario = ownerId,
            titulo = titulo,
            descricao = descricao,
            status = status,
            prioridade = prioridade,
            dataVencimento = dataVencimento,
            dataCriacao = dataCriacao,
            dataAtualizacao = dataAtualizacao,
            dataConclusao = dataConclusao,
        )
    }

    override fun listByOwner(ownerId: UUID): List<TaskRecord> = transaction {
        TasksTable
            .selectAll()
            .where { TasksTable.idUsuario eq ownerId }
            .orderBy(TasksTable.dataCriacao to SortOrder.DESC)
            .map { it.toTaskRecord() }
    }

    override fun findById(taskId: UUID): TaskRecord? = transaction {
        TasksTable
            .selectAll()
            .where { TasksTable.id eq taskId }
            .singleOrNull()
            ?.toTaskRecord()
    }

    override fun updateStatusByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        status: TaskStatus,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord? = transaction {
        val updatedRows = TasksTable.update({ (TasksTable.id eq taskId) and (TasksTable.idUsuario eq ownerId) }) {
            it[this.status] = status
            it[this.dataAtualizacao] = dataAtualizacao
            it[this.dataConclusao] = dataConclusao
        }

        if (updatedRows == 0) {
            null
        } else {
            TasksTable
                .selectAll()
                .where { (TasksTable.id eq taskId) and (TasksTable.idUsuario eq ownerId) }
                .single()
                .toTaskRecord()
        }
    }

    private fun ResultRow.toTaskRecord(): TaskRecord = TaskRecord(
        id = this[TasksTable.id].value,
        idUsuario = this[TasksTable.idUsuario].value,
        titulo = this[TasksTable.titulo],
        descricao = this[TasksTable.descricao],
        status = this[TasksTable.status],
        prioridade = this[TasksTable.prioridade],
        dataVencimento = this[TasksTable.dataVencimento],
        dataCriacao = this[TasksTable.dataCriacao],
        dataAtualizacao = this[TasksTable.dataAtualizacao],
        dataConclusao = this[TasksTable.dataConclusao],
    )
}

