package com.adrianobispo.unit

import com.adrianobispo.shared.ApiException
import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import com.adrianobispo.tasks.TaskRecord
import com.adrianobispo.tasks.TaskRepository
import com.adrianobispo.tasks.TaskListQuery
import com.adrianobispo.tasks.TaskListResult
import com.adrianobispo.tasks.TaskService
import com.adrianobispo.tasks.UpdateTaskRequestDto
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskEditDeleteServiceTest {
    @Test
    fun `partial update validates title length`() {
        val ownerId = UUID.randomUUID()
        val task = baseTask(ownerId)
        val repository = EditDeleteFakeTaskRepository(task)
        val service = TaskService(repository)

        val error = runCatching {
            service.updateTask(ownerId, task.id, UpdateTaskRequestDto(titulo = "x"))
        }.exceptionOrNull() as? ApiException

        assertNotNull(error)
        assertEquals(HttpStatusCode.BadRequest, error.status)
        assertTrue(error.message.contains("Título"))
        assertEquals("Tarefa original", repository.records[task.id]?.titulo)
    }

    @Test
    fun `partial update returns not found for missing task`() {
        val repository = EditDeleteFakeTaskRepository()
        val service = TaskService(repository)

        val error = runCatching {
            service.updateTask(UUID.randomUUID(), UUID.randomUUID(), UpdateTaskRequestDto(titulo = "Nova tarefa"))
        }.exceptionOrNull() as? ApiException

        assertNotNull(error)
        assertEquals(HttpStatusCode.NotFound, error.status)
    }

    @Test
    fun `delete rejects non owner`() {
        val ownerId = UUID.randomUUID()
        val otherId = UUID.randomUUID()
        val task = baseTask(ownerId)
        val repository = EditDeleteFakeTaskRepository(task)
        val service = TaskService(repository)

        val error = runCatching {
            service.delete(otherId, task.id)
        }.exceptionOrNull() as? ApiException

        assertNotNull(error)
        assertEquals(HttpStatusCode.Forbidden, error.status)
        assertTrue(repository.records.containsKey(task.id))
    }

    @Test
    fun `delete removes task for owner`() {
        val ownerId = UUID.randomUUID()
        val task = baseTask(ownerId)
        val repository = EditDeleteFakeTaskRepository(task)
        val service = TaskService(repository)

        service.delete(ownerId, task.id)

        assertTrue(!repository.records.containsKey(task.id))
    }

    private fun baseTask(ownerId: UUID): TaskRecord {
        val now = Instant.now()
        return TaskRecord(
            id = UUID.randomUUID(),
            idUsuario = ownerId,
            titulo = "Tarefa original",
            descricao = "Descrição",
            status = TaskStatus.PENDENTE,
            prioridade = TaskPriority.MEDIA,
            dataVencimento = null,
            dataCriacao = now,
            dataAtualizacao = now,
            dataConclusao = null,
        )
    }
}

private class EditDeleteFakeTaskRepository(vararg tasks: TaskRecord) : TaskRepository {
    val records: MutableMap<UUID, TaskRecord> = tasks.associateBy { it.id }.toMutableMap()

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
    ): TaskRecord = TaskRecord(
        id = UUID.randomUUID(),
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

    override fun listByOwner(ownerId: UUID): List<TaskRecord> = records.values.filter { it.idUsuario == ownerId }

    override fun listByOwner(ownerId: UUID, query: TaskListQuery): TaskListResult {
        val all = listByOwner(ownerId)
        return TaskListResult(items = all, totalItems = all.size)
    }

    override fun findById(taskId: UUID): TaskRecord? = records[taskId]

    override fun updateStatusByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        status: TaskStatus,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord? {
        val existing = records[taskId] ?: return null
        return updateByIdAndOwner(
            taskId,
            ownerId,
            existing.titulo,
            existing.descricao,
            status,
            existing.prioridade,
            existing.dataVencimento,
            dataAtualizacao,
            dataConclusao,
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
    ): TaskRecord? {
        val existing = records[taskId] ?: return null
        if (existing.idUsuario != ownerId) return null

        return existing.copy(
            titulo = titulo,
            descricao = descricao,
            status = status,
            prioridade = prioridade,
            dataVencimento = dataVencimento,
            dataAtualizacao = dataAtualizacao,
            dataConclusao = dataConclusao,
        ).also { records[taskId] = it }
    }

    override fun deleteByIdAndOwner(taskId: UUID, ownerId: UUID): Boolean {
        val existing = records[taskId] ?: return false
        if (existing.idUsuario != ownerId) return false
        records.remove(taskId)
        return true
    }
}


