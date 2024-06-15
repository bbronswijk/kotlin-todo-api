package nl.brambronswijk.todo_api.model

import java.util.UUID

interface Response<T> {
    val data: T
}

data class GetTodosResponse(override val data: List<Todo>): Response<List<Todo>>
data class CreateTodoResponse(override val data: Todo): Response<Todo>
data class CreateManyTodoResponse(override val data: List<Todo>): Response<List<Todo>>
data class PatchTodoResponse(override val data: Todo?): Response<Todo?>
data class DeleteTodoResponse(override val data: UUID): Response<UUID>
data class DeleteManyTodosResponse(override val data: List<UUID>): Response<List<UUID>>

