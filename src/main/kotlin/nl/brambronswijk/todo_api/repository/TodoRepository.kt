package nl.brambronswijk.todo_api.repository

import nl.brambronswijk.todo_api.model.Todo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID


interface TodoRepository : CoroutineCrudRepository<Todo, UUID>