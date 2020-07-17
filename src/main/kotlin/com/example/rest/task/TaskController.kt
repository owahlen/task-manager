package com.example.rest.task

import com.example.rest.model.Task
import com.example.rest.model.TaskDTO
import com.example.rest.user.UserService
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
//@Validated
@RequestMapping("/api")
class TaskController(private val taskService: TaskService, private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(TaskController::class.java)

    @GetMapping("/task")
    suspend fun findAll(): Flow<Task> {
        return taskService.findAll()
    }

    @GetMapping("/task/search")
    suspend fun search(@RequestParam criterion: Map<String, String?>): Flow<Task> {
        if (criterion.containsKey("completed")) {
            val completed = criterion.get("completed")
            if (completed == null || completed != "true" && completed != "false") {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "search criterion for 'completed' must be 'true' or 'false'")
            }
            return taskService.findByCompleted(completed.toBoolean())
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The search parameter 'completed' must be provided")
        }
    }

    @GetMapping("/task/{id}")
    suspend fun findTask(@PathVariable id: String): Task {
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "task id must be a positive integer")
        return taskService.findById(entityId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '${entityId}' could not found")
    }

    @PostMapping("/task")
    suspend fun createTask(@Valid @RequestBody taskDTO: TaskDTO): Task {
        return taskService.create(taskDTO)
    }

    @PutMapping("/task/{id}")
    suspend fun updateTask(@PathVariable id: String, @Valid @RequestBody taskDTO: TaskDTO): Task {
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "task id must be a positive integer")
        return taskService.update(entityId, taskDTO)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '$id' not found")
    }

    @DeleteMapping("/task/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteTask(@PathVariable id: String) {
        val principal = userService.getCurrentUser()
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "task id must be a positive integer")
        if (!taskService.delete(entityId)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '$id' not found")
    }
}
