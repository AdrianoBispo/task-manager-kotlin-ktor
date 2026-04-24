package com.adrianobispo.unit

import com.adrianobispo.shared.ApiException
import com.adrianobispo.shared.TaskPriority
import com.adrianobispo.shared.TaskStatus
import com.adrianobispo.tasks.TaskListQuery
import com.adrianobispo.tasks.TaskListResult
import com.adrianobispo.tasks.TaskRecord
import com.adrianobispo.tasks.TaskRepository
import com.adrianobispo.tasks.TaskService
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TaskQuerySpecTest {
    @Test
    fun `normalizes query parameters and computes paginated meta`() {
        val ownerId = UUID.randomUUID()
        val repository = QueryFakeTaskRepository(
            TaskListResult(
                items = listOf(baseTask(ownerId, "A"), baseTask(ownerId, "B")),
                totalItems = 5,
            ),
        )
        val service = TaskService(repository)

        val response = service.listByOwner(
            ownerId = ownerId,
            status = "pendente",
            prioridade = "alta",
            q = "  relatorio  ",
            sort = "data_vencimento",
            page = "2",
            limit = "2",
        )

        val query = repository.lastQuery
        assertNotNull(query)
        assertEquals(TaskStatus.PENDENTE, query.status)
        assertEquals(TaskPriority.ALTA, query.prioridade)
        assertEquals("relatorio", query.q)
        assertEquals(2, query.page)
        assertEquals(2, query.limit)

        assertEquals(5, response.meta.totalItens)
        assertEquals(3, response.meta.totalPaginas)
        assertEquals(2, response.meta.paginaAtual)
        assertEquals(2, response.meta.itensPorPagina)
        assertEquals(2, response.dados.size)
    }

    @Test
    fun `defaults query values when params are absent`() {
        val ownerId = UUID.randomUUID()
        val repository = QueryFakeTaskRepository(TaskListResult(items = emptyList(), totalItems = 0))
        val service = TaskService(repository)

        val response = service.listByOwner(ownerId)

        val query = repository.lastQuery
        assertNotNull(query)
        assertNull(query.status)
        assertNull(query.prioridade)
        assertNull(query.q)
        assertEquals(1, query.page)
        assertEquals(10, query.limit)
        assertEquals(0, response.meta.totalPaginas)
        assertEquals(10, response.meta.itensPorPagina)
    }

    @Test
    fun `rejects invalid sort parameter`() {
        val ownerId = UUID.randomUUID()
        val service = TaskService(QueryFakeTaskRepository(TaskListResult(items = emptyList(), totalItems = 0)))

        val error = runCatching {
            service.listByOwner(ownerId, null, null, null, "-prioridade", null, null)
        }.exceptionOrNull() as? ApiException

        assertNotNull(error)
        assertEquals(HttpStatusCode.BadRequest, error.status)
    }

    private fun baseTask(ownerId: UUID, titulo: String): TaskRecord {
        val now = Instant.now()
        return TaskRecord(
            id = UUID.randomUUID(),
            idUsuario = ownerId,
            titulo = titulo,
            descricao = null,
            status = TaskStatus.PENDENTE,
            prioridade = TaskPriority.MEDIA,
            dataVencimento = null,
            dataCriacao = now,
            dataAtualizacao = now,
            dataConclusao = null,
        )
    }
}

private class QueryFakeTaskRepository(
    private val queryResult: TaskListResult,
) : TaskRepository {
    var lastQuery: TaskListQuery? = null

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
    ): TaskRecord = error("Not needed")

    override fun listByOwner(ownerId: UUID): List<TaskRecord> = queryResult.items

    override fun listByOwner(ownerId: UUID, query: TaskListQuery): TaskListResult {
        lastQuery = query
        return queryResult
    }

    override fun findById(taskId: UUID): TaskRecord? = null

    override fun updateStatusByIdAndOwner(
        taskId: UUID,
        ownerId: UUID,
        status: TaskStatus,
        dataAtualizacao: Instant,
        dataConclusao: Instant?,
    ): TaskRecord? = null

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
    ): TaskRecord? = null

    override fun deleteByIdAndOwner(taskId: UUID, ownerId: UUID): Boolean = false
}

