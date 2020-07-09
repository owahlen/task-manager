package com.example.rest.task

import com.example.rest.model.ErrorMessage
import com.example.rest.model.TaskDTO
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*

@Component
class TaskHandler(val taskService: TaskService) {
    private val logger = LoggerFactory.getLogger(TaskHandler::class.java)

    suspend fun findAll(request: ServerRequest): ServerResponse {
        val tasks = taskService.findAll()
        return ServerResponse.ok().json().bodyAndAwait(tasks)
    }

    suspend fun search(request: ServerRequest): ServerResponse {
        val criteria = request.queryParams()
        return when {
            criteria.isEmpty() -> ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Search must have query params"))
            criteria.contains("completed") -> {
                val criteriaValue = criteria.getFirst("completed")
                if (criteriaValue.isNullOrBlank()) {
                    ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Incorrect search criteria value"))
                } else {
                    ServerResponse.ok().json().bodyAndAwait(taskService.findByCompleted(criteriaValue.toBoolean()))
                }
            }
            else -> ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Incorrect search criteria"))
        }
    }

    suspend fun findTask(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val task = taskService.findById(id)
            if (task == null) ServerResponse.notFound().buildAndAwait()
            else ServerResponse.ok().json().bodyValueAndAwait(task)
        }
    }

    suspend fun createTask(request: ServerRequest): ServerResponse {
        val newTask = try {
            request.bodyToMono<TaskDTO>().awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Decoding body error", e)
            null
        }
        return if (newTask == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
        } else {
            val user = taskService.create(newTask)
            if (user == null) ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).json().bodyValueAndAwait(ErrorMessage("Internal error"))
            else ServerResponse.status(HttpStatus.CREATED).json().bodyValueAndAwait(user)
        }
    }

    suspend fun updateTask(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            val updateTask = try {
                request.bodyToMono<TaskDTO>().awaitFirstOrNull()
            } catch (e: Exception) {
                logger.error("Decoding body error", e)
                null
            }
            if (updateTask == null) {
                ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("Invalid body"))
            } else {
                val task = taskService.update(id, updateTask)
                if (task == null) ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
                else ServerResponse.status(HttpStatus.OK).json().bodyValueAndAwait(task)
            }
        }
    }

    suspend fun deleteTask(request: ServerRequest): ServerResponse {
        val id = request.pathVariable("id").toLongOrNull()
        return if (id == null) {
            ServerResponse.badRequest().json().bodyValueAndAwait(ErrorMessage("`id` must be numeric"))
        } else {
            if (taskService.delete(id)) ServerResponse.noContent().buildAndAwait()
            else ServerResponse.status(HttpStatus.NOT_FOUND).json().bodyValueAndAwait(ErrorMessage("Resource $id not found"))
        }
    }
}
