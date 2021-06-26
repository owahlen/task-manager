package org.taskmanager.task.controller


import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.taskmanager.task.service.TaskService
import org.taskmanager.task.service.UserService
import org.taskmanager.task.service.dto.TaskDTO
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1")
class TaskController(private val taskService: TaskService, private val userService: UserService) {
    private val log = LoggerFactory.getLogger(TaskController::class.java)

    @GetMapping("/task")
    suspend fun findAll(): Flow<TaskDTO> {
        return taskService.findAll()
    }

    @GetMapping("/task/search")
    suspend fun search(@RequestParam criterionMap: Map<String, String?>): Flow<TaskDTO> {
        if (criterionMap.size != 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "There must be one search criterion out of: " +
                    enumValues<SearchCriterion>().joinToString { "'${it.name}'" })
        }
        val criterion = criterionMap.keys.first()
        when (criterion) {
            "description" -> {
                val description = criterionMap[criterion]
                if (description.isNullOrBlank()) {
                    return taskService.findAll()
                }
                return taskService.findByDescription(description)
            }
            "completed" -> {
                val completed = criterionMap[criterion]
                if (completed == null || completed != "true" && completed != "false") {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "search criterion for 'completed' must be 'true' or 'false'"
                    )
                }
                return taskService.findByCompleted(completed.toBoolean())
            }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The search parameter must be one of " +
                    enumValues<SearchCriterion>().joinToString { "'${it.name}'" })
        }
    }

    @GetMapping("/task/{id}")
    suspend fun findTask(@PathVariable id: String): TaskDTO {
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "task id must be a positive integer"
        )
        return taskService.findById(entityId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '${entityId}' could not found")
    }

    @PostMapping("/task")
    suspend fun createTask(@Valid @RequestBody taskDTO: TaskDTO): TaskDTO {
        if (taskDTO.id != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id property must not be set for task")
        }
        return taskService.create(taskDTO)
    }

    @PutMapping("/task/{id}")
    suspend fun updateTask(@PathVariable id: String, @Valid @RequestBody taskDTO: TaskDTO): TaskDTO {
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "task id must be a positive integer"
        )
        if (taskDTO.id != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id property must not be set for task")
        }
        return taskService.update(taskDTO.copy(id = entityId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '$id' not found")
    }

//    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/task/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteTask(@PathVariable id: String) {
        val principal = userService.getCurrentUser()
        val entityId = id.toLongOrNull()
        if (entityId == null || entityId < 1) throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "task id must be a positive integer"
        )
        if (!taskService.delete(entityId)) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "task with id '$id' not found"
        )
    }

    private enum class SearchCriterion { description, completed }
}
