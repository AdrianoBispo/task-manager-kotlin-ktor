package com.adrianobispo.tasks

import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import com.adrianobispo.shared.TasksTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
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

enum class TaskSortField {
    DATA_VENCIMENTO,
    DATA_CRIACAO,
}

enum class TaskSortDirection {
    ASC,
    DESC,
}

data class TaskSort(
    val field: TaskSortField,
    val direction: TaskSortDirection,
)

data class TaskListQuery(
    val status: TaskStatus? = null,
    val prioridade: TaskPriority? = null,
    val q: String? = null,
    val sort: TaskSort = TaskSort(field = TaskSortField.DATA_CRIACAO, direction = TaskSortDirection.DESC),
    val page: Int = 1,
    val limit: Int = 10,
)

data class TaskListResult(
    val items: List<TaskRecord>,
    val totalItems: Int,
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

    fun listByOwner(ownerId: UUID, query: TaskListQuery): TaskListResult

    fun findById(taskId: UUID): TaskRecord?

    fun updateStatusByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        status: TaskStatus,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord?

    fun updateByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        titulo: String,
        descricao: String?,
        status: TaskStatus,
        prioridade: TaskPriority,
        dataVencimento: Instant?,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord?

    fun deleteByIdAndOwner(taskId: UUID, ownerId: UUID): Boolean
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

    override fun listByOwner(ownerId: UUID, query: TaskListQuery): TaskListResult = transaction {
        val filtered = TasksTable
            .selectAll()
            .where { TasksTable.idUsuario eq ownerId }
            .map { it.toTaskRecord() }
            .asSequence()
            .filter { query.status == null || it.status == query.status }
            .filter { query.prioridade == null || it.prioridade == query.prioridade }
            .filter { record ->
                val term = query.q ?: return@filter true
                record.titulo.contains(term, ignoreCase = true) ||
                    (record.descricao?.contains(term, ignoreCase = true) == true)
            }
            .toList()

        val sorted = filtered.sortedWith(buildComparator(query.sort))
        val offset = (query.page - 1) * query.limit
        val pageItems = if (offset >= sorted.size) {
            emptyList()
        } else {
            sorted.drop(offset).take(query.limit)
        }

        TaskListResult(
            items = pageItems,
            totalItems = filtered.size,
        )
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
        val current = TasksTable
            .selectAll()
            .where { (TasksTable.id eq taskId) and (TasksTable.idUsuario eq ownerId) }
            .singleOrNull()
            ?.toTaskRecord()
            ?: return@transaction null

        updateByIdAndOwner(
            taskId = taskId,
            ownerId = ownerId,
            titulo = current.titulo,
            descricao = current.descricao,
            status = status,
            prioridade = current.prioridade,
            dataVencimento = current.dataVencimento,
            dataAtualizacao = dataAtualizacao,
            dataConclusao = dataConclusao,
        )
    }

    override fun updateByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        titulo: String,
        descricao: String?,
        status: TaskStatus,
        prioridade: TaskPriority,
        dataVencimento: Instant?,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord? = transaction {
        val updatedRows = TasksTable.update({ (TasksTable.id eq taskId) and (TasksTable.idUsuario eq ownerId) }) {
            it[this.titulo] = titulo
            it[this.descricao] = descricao
            it[this.status] = status
            it[this.prioridade] = prioridade
            it[this.dataVencimento] = dataVencimento
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

    override fun deleteByIdAndOwner(taskId: UUID, ownerId: UUID): Boolean = transaction {
        TasksTable.deleteWhere { (TasksTable.id eq taskId) and (TasksTable.idUsuario eq ownerId) } > 0
    }

    private fun buildComparator(sort: TaskSort): Comparator<TaskRecord> {
        val comparator = when (sort.field) {
            TaskSortField.DATA_CRIACAO -> compareBy<TaskRecord> { it.dataCriacao }
            TaskSortField.DATA_VENCIMENTO -> compareBy<TaskRecord> { it.dataVencimento ?: Instant.MAX }
        }.thenByDescending { it.dataCriacao }

        return if (sort.direction == TaskSortDirection.ASC) comparator else comparator.reversed()
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

