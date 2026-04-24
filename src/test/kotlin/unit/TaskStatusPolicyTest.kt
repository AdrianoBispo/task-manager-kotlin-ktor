package com.adrianobispo.unit

import com.adrianobispo.shared.ApiException
import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import com.adrianobispo.tasks.TaskRecord
import com.adrianobispo.tasks.TaskRepository
import com.adrianobispo.tasks.TaskService
import com.adrianobispo.tasks.UpdateTaskStatusRequestDto
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskStatusPolicyTest {
    @Test
    fun `allows valid transition to CONCLUIDA and sets data_conclusao`() {
        val ownerId = UUID.randomUUID()
        val task = baseTask(ownerId = ownerId, status = TaskStatus.PENDENTE, dataConclusao = null)
        val repository = FakeTaskRepository(task)
        val service = TaskService(repository)

        val updated = service.updateStatus(ownerId, task.id, UpdateTaskStatusRequestDto(status = TaskStatus.CONCLUIDA))

        assertEquals(TaskStatus.CONCLUIDA, updated.status)
        assertNotNull(updated.dataConclusao)
        assertEquals(TaskStatus.CONCLUIDA, repository.records[task.id]?.status)
        assertNotNull(repository.records[task.id]?.dataConclusao)
    }

    @Test
    fun `allows transition away from CONCLUIDA and clears data_conclusao`() {
        val ownerId = UUID.randomUUID()
        val task = baseTask(ownerId = ownerId, status = TaskStatus.CONCLUIDA, dataConclusao = Instant.now())
        val repository = FakeTaskRepository(task)
        val service = TaskService(repository)

        val updated = service.updateStatus(ownerId, task.id, UpdateTaskStatusRequestDto(status = TaskStatus.EM_ANDAMENTO))

        assertEquals(TaskStatus.EM_ANDAMENTO, updated.status)
        assertNull(updated.dataConclusao)
        assertNull(repository.records[task.id]?.dataConclusao)
    }

    @Test
    fun `rejects disallowed transition from CONCLUIDA to PENDENTE`() {
        val ownerId = UUID.randomUUID()
        val task = baseTask(ownerId = ownerId, status = TaskStatus.CONCLUIDA, dataConclusao = Instant.now())
        val repository = FakeTaskRepository(task)
        val service = TaskService(repository)

        val error = kotlin.runCatching {
            service.updateStatus(ownerId, task.id, UpdateTaskStatusRequestDto(status = TaskStatus.PENDENTE))
        }.exceptionOrNull() as? ApiException

        assertNotNull(error)
        assertEquals(HttpStatusCode.BadRequest, error.status)
        assertTrue(error.message.contains("Transição de status"))
        assertEquals(TaskStatus.CONCLUIDA, repository.records[task.id]?.status)
    }

    private fun baseTask(ownerId: UUID, status: TaskStatus, dataConclusao: Instant?): TaskRecord {
        val now = Instant.now()
        return TaskRecord(
            id = UUID.randomUUID(),
            idUsuario = ownerId,
            titulo = "Tarefa",
            descricao = null,
            status = status,
            prioridade = TaskPriority.MEDIA,
            dataVencimento = null,
            dataCriacao = now,
            dataAtualizacao = now,
            dataConclusao = dataConclusao,
        )
    }
}

private class FakeTaskRepository(task: TaskRecord) : TaskRepository {
    val records: MutableMap<UUID, TaskRecord> = mutableMapOf(task.id to task)

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
    ): TaskRecord = error("Not needed for this test")

    override fun listByOwner(ownerId: UUID): List<TaskRecord> = records.values.filter { it.idUsuario == ownerId }

    override fun findById(taskId: UUID): TaskRecord? = records[taskId]

    override fun updateStatusByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        status: TaskStatus,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord? {
        val existing = records[taskId] ?: return null
        if (existing.idUsuario != ownerId) return null
        return existing.copy(
            status = status,
            dataAtualizacao = dataAtualizacao,
            dataConclusao = dataConclusao,
        ).also { records[taskId] = it }
    }
}

