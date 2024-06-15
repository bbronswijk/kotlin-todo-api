package nl.brambronswijk.todo_api.api

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
    suspend fun find(@RequestParam offset: Int?, @RequestParam limit: Int?): GetTodosResponse {
        val todos: List<Todo> = databaseClient.sql("SELECT * FROM todo.todo OFFSET :offset LIMIT :limit")
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

        return GetTodosResponse(data = todos);
    }

    /**
     * Create a new todo
     */
    @PostMapping("/todos")
    suspend fun create(@RequestBody todo: CreateTodoRequest): CreateTodoResponse {
        val created: Todo = todoRepository.save(
            Todo(title = todo.title, completed = todo.completed)
        )

        return CreateTodoResponse(data = created)
    }

    /**
     * Create many todos at once
     */
    @PostMapping("/todos/import")
    suspend fun createMany(@RequestBody request: CreateManyTodosRequest): CreateManyTodoResponse {
        val todos: List<Todo> = request.todos
            .map { todo -> Todo(title = todo.title, completed = todo.completed)}

        val created: List<Todo> = todoRepository.saveAll(todos).toList()

        return CreateManyTodoResponse(data = created)
    }

    /**
     * Update a todo
     */
    @PatchMapping("/todos/{todoId}")
    suspend fun update(@PathVariable todoId: UUID, @RequestBody todo: UpdateTodoRequest): PatchTodoResponse {
        val existingTodo = todoRepository.findById(todoId) ?: return PatchTodoResponse(data = null)

        val updated = todoRepository.save(
            existingTodo.copy(
                title = todo.title ?: existingTodo.title,
                completed = todo.completed
            )
        )

        return PatchTodoResponse(data = updated)
    }

    /**
     * Delete a todo
     */
    @DeleteMapping("/todos/{todoId}")
    suspend fun delete(@PathVariable todoId: UUID): DeleteTodoResponse {
        todoRepository.deleteById(todoId)

        return DeleteTodoResponse(todoId)
    }
    /**
     * Delete many todos at once
     */
    @DeleteMapping("/todos")
    suspend fun deleteMany(@RequestBody request: DeleteManyTodosRequest): DeleteManyTodosResponse {
        todoRepository.deleteAllById(request.deleteIds)

        return DeleteManyTodosResponse(data = request.deleteIds)
    }
}