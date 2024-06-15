package nl.brambronswijk.todo_api.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import nl.brambronswijk.todo_api.model.*
import nl.brambronswijk.todo_api.repository.TodoRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.flow
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api")
class TodoHandler(private val databaseClient: DatabaseClient, private val todoRepository: TodoRepository) {

    /**
     * Find todos with pagination. Use custom SQL since
     * CoroutineCrudRepository does not support pagination.
     */
    @GetMapping("/todos")
    suspend fun find(@RequestParam offset: Int?, @RequestParam limit: Int?): List<Todo> {
        return databaseClient.sql("SELECT * FROM todo.todo OFFSET :offset LIMIT :limit")
            .bind("offset", offset ?: 0)
            .bind("limit", limit ?: 10)
            .fetch()
            .flow()
            .toList()
            .map {
                Todo(
                id = it["id"] as UUID,
                title = it["title"] as String,
                completed = it["completed"] as Boolean
            ) }
    }

    /**
     * Create a new todo
     */
    @PostMapping("/todos")
    suspend fun create(@RequestBody todo: CreateRequest): Todo {
        return todoRepository.save(
            Todo(title = todo.title, completed = todo.completed)
        )
    }

    /**
     * Create many todos at once
     */
    @PostMapping("/todos/import")
    suspend fun createMany(@RequestBody request: List<CreateRequest>): Flow<Todo> {
        val todos: List<Todo> = request
            .map { todo -> Todo(title = todo.title, completed = todo.completed)}

        return todoRepository.saveAll(todos)
    }

    /**
     * Update a todo
     */
    @PatchMapping("/todos/{todoId}")
    suspend fun update(@PathVariable todoId: UUID, @RequestBody todo: UpdateRequest): Todo? {
        val existingTodo = todoRepository.findById(todoId) ?: return null

        val updated = todoRepository.save(
            existingTodo.copy(
                title = todo.title ?: existingTodo.title,
                completed = todo.completed
            )
        )

        return todoRepository.save(updated)
    }

    /**
     * Delete a todo
     */
    @DeleteMapping("/todos/{todoId}")
    suspend fun delete(@PathVariable todoId: UUID): DeleteResponse {
        todoRepository.deleteById(todoId)

        return DeleteResponse(todoId)
    }
    /**
     * Delete many todos at once
     */
    @DeleteMapping("/todos")
    suspend fun deleteMany(@RequestBody request: DeleteManyRequest): List<UUID> {
        todoRepository.deleteAllById(request.deleteIds)

        return request.deleteIds
    }
}