package nl.brambronswijk.todo_api.model

import java.util.UUID

data class CreateTodoRequest(
    val title: String,
    val completed: Boolean,
)

data class CreateManyTodosRequest(val todos: List<CreateTodoRequest>)

data class UpdateTodoRequest(
    val title: String?,
    val completed: Boolean,
)

data class DeleteManyTodosRequest(val deleteIds: List<UUID>)