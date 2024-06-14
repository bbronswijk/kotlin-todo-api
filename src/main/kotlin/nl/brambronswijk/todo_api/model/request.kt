package nl.brambronswijk.todo_api.model

import java.util.UUID

data class CreateRequest(
    val title: String,
    val completed: Boolean,
)

data class UpdateRequest(
    val title: String?,
    val completed: Boolean,
)

data class DeleteManyRequest(val deleteIds: List<UUID>)