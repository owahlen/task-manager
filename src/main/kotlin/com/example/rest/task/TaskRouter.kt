package com.example.rest.task

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter


@Configuration
class TaskRouter {

    @Bean
    fun taskRoute(taskHandler: TaskHandler) = coRouter {
        GET("/api/task", taskHandler::findAll)
        GET("/api/task/search", taskHandler::search)
        GET("/api/task/{id}", taskHandler::findTask)
        POST("/api/task", taskHandler::createTask)
        PUT("/api/task/{id}", taskHandler::updateTask)
        DELETE("/api/task/{id}", taskHandler::deleteTask)
    }

}
