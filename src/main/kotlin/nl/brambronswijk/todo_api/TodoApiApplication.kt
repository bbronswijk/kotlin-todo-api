package nl.brambronswijk.todo_api

import io.r2dbc.spi.ConnectionFactory
import nl.brambronswijk.todo_api.api.TodoHandler
import nl.brambronswijk.todo_api.repository.TodoRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.EntityResponse.fromObject

@SpringBootApplication
@EnableR2dbcRepositories
class TodoApiApplication

fun main(args: Array<String>) {
	runApplication<TodoApiApplication>(*args) {
		addInitializers(beans)
	}
}

val beans = beans {
	bean(::routes)
	bean<TodoHandler>()
	bean(::corsSettings)
}

fun routes(
	todoHandler: TodoHandler,
) = coRouter {
	"api".nest {
		GET("/todos", todoHandler::find)
		POST("/todos", todoHandler::create)
		POST("/todos/import", todoHandler::createMany)
		PATCH("/todos/{id}", todoHandler::update)
		DELETE("/todos/{id}", todoHandler::delete)
		DELETE("/todos", todoHandler::deleteMany)
	}
}

fun corsSettings(): CorsWebFilter {
	val config = CorsConfiguration()

	config.addAllowedOrigin("http://localhost:3000")
	config.addAllowedHeader("*")
	config.addAllowedMethod("*")

	val source = UrlBasedCorsConfigurationSource().apply {
		registerCorsConfiguration("/**", config)
	}

	return CorsWebFilter(source)
}
