package nl.brambronswijk.todo_api.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "todo", schema = "todo")
data class Todo(
    @Id val id: UUID? = null,
    val title: String,
    val completed: Boolean,
)
