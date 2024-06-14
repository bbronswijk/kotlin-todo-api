package nl.brambronswijk.todo_api.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import nl.brambronswijk.todo_api.model.*
import nl.brambronswijk.todo_api.repository.TodoRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.flow
import org.springframework.web.reactive.function.server.*
import java.util.*

class TodoHandler(private val databaseClient: DatabaseClient, private val todoRepository: TodoRepository) {
    /**
     * Find todos with pagination. Use custom SQL since
     * CoroutineCrudRepository does not support pagination.
     */
    suspend fun find(request: ServerRequest): ServerResponse {
        val offset: Int = request.queryParam("offset").orElse("0").toInt()
        val limit: Int = request.queryParam("limit").orElse("10").toInt()

        val todos: List<Todo> = databaseClient.sql("SELECT * FROM todo.todo OFFSET :offset LIMIT :limit")
            .bind("offset", offset)
            .bind("limit", limit)
            .fetch()
            .flow()
            .toList()
            .map {
                Todo(
                id = it["id"] as UUID,
                title = it["title"] as String,
                completed = it["completed"] as Boolean
            ) }

        return ServerResponse.ok().bodyValueAndAwait(todos)
    }

    /**
     * Create a new todo
     */
    suspend fun create(request: ServerRequest): ServerResponse {
        val todo: CreateRequest = request.awaitBody<CreateRequest>()

        val created: Todo = todoRepository.save(
            Todo(title = todo.title, completed = todo.completed)
        )

        return ServerResponse.ok().bodyValueAndAwait(created)
    }

    /**
     * Create many todos at once
     */
    suspend fun createMany(request: ServerRequest): ServerResponse {
        val todos: List<Todo> = request
            .awaitBody<List<CreateRequest>>()
            .map { todo -> Todo(title = todo.title, completed = todo.completed)}

        val created: Flow<Todo> = todoRepository.saveAll(todos)

        return ServerResponse.ok().bodyValueAndAwait(created.toList())
    }

    /**
     * Update a todo
     */
    suspend fun update(request: ServerRequest): ServerResponse {
        val todoId: UUID = UUID.fromString(request.pathVariable("id"))
        val todo: UpdateRequest = request.awaitBody<UpdateRequest>()

        val existingTodo = todoRepository.findById(todoId) ?: return ServerResponse.notFound().buildAndAwait()

        val updated = todoRepository.save(
            existingTodo.copy(
                title = todo.title ?: existingTodo.title,
                completed = todo.completed
            )
        )

        val saved: Todo = todoRepository.save(updated)

        return ServerResponse.ok().bodyValueAndAwait(saved)
    }

    /**
     * Delete a todo
     */
    suspend fun delete(request: ServerRequest): ServerResponse {
        val todoId: UUID = UUID.fromString(request.pathVariable("id"))

        todoRepository.deleteById(todoId)

        return ServerResponse.ok().bodyValueAndAwait(DeleteResponse(todoId))
    }
    /**
     * Delete many todos at once
     */
    suspend fun deleteMany(request: ServerRequest): ServerResponse {
        val deleteIds: List<UUID> = request.awaitBody<DeleteManyRequest>().deleteIds

        todoRepository.deleteAllById(deleteIds)

        return ServerResponse.ok().bodyValueAndAwait(deleteIds)
    }
}