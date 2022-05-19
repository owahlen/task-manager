package org.taskmanager.task.api.controller


import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.taskmanager.task.service.TaskServiceOld
import org.taskmanager.task.service.UserServiceOld

@RestController
@RequestMapping("/api/v1")
class TaskControllerOld(private val taskService: TaskServiceOld, private val userService: UserServiceOld) {
    private val log = LoggerFactory.getLogger(TaskControllerOld::class.java)

//    @GetMapping("/task")
//    suspend fun findAll(): Flow<TaskDTO> {
//        return taskService.findAll()
//    }
//
//    @GetMapping("/task/search")
//    suspend fun search(@RequestParam criterionMap: Map<String, String?>): Flow<TaskDTO> {
//        if (criterionMap.size != 1) {
//            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "There must be one search criterion out of: " +
//                    enumValues<SearchCriterion>().joinToString { "'${it.name}'" })
//        }
//        val criterion = criterionMap.keys.first()
//        when (criterion) {
//            "description" -> {
//                val description = criterionMap[criterion]
//                if (description.isNullOrBlank()) {
//                    return taskService.findAll()
//                }
//                return taskService.findByDescription(description)
//            }
//            "completed" -> {
//                val completed = criterionMap[criterion]
//                if (completed == null || completed != "true" && completed != "false") {
//                    throw ResponseStatusException(
//                        HttpStatus.BAD_REQUEST,
//                        "search criterion for 'completed' must be 'true' or 'false'"
//                    )
//                }
//                return taskService.findByCompleted(completed.toBoolean())
//            }
//            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The search parameter must be one of " +
//                    enumValues<SearchCriterion>().joinToString { "'${it.name}'" })
//        }
//    }
//
//    @GetMapping("/task/{id}")
//    suspend fun findTask(@PathVariable id: String): TaskDTO {
//        val entityId = id.toLongOrNull()
//        if (entityId == null || entityId < 1) throw ResponseStatusException(
//            HttpStatus.BAD_REQUEST,
//            "task id must be a positive integer"
//        )
//        return taskService.findById(entityId)
//            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '${entityId}' could not found")
//    }
//
//    @PostMapping("/task")
//    suspend fun createTask(@Valid @RequestBody taskDTO: TaskDTO): TaskDTO {
//        if (taskDTO.id != null) {
//            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id property must not be set for task")
//        }
//        return taskService.create(taskDTO)
//    }
//
//    @PutMapping("/task/{id}")
//    suspend fun updateTask(@PathVariable id: String, @Valid @RequestBody taskDTO: TaskDTO): TaskDTO {
//        val entityId = id.toLongOrNull()
//        if (entityId == null || entityId < 1) throw ResponseStatusException(
//            HttpStatus.BAD_REQUEST,
//            "task id must be a positive integer"
//        )
//        if (taskDTO.id != null) {
//            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "id property must not be set for task")
//        }
//        return taskService.update(taskDTO.copy(id = entityId))
//            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "task with id '$id' not found")
//    }
//
////    @PreAuthorize("isAuthenticated()")
//    @DeleteMapping("/task/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    suspend fun deleteTask(@PathVariable id: String) {
//        val principal = userService.getCurrentUser()
//        val entityId = id.toLongOrNull()
//        if (entityId == null || entityId < 1) throw ResponseStatusException(
//            HttpStatus.BAD_REQUEST,
//            "task id must be a positive integer"
//        )
//        if (!taskService.delete(entityId)) throw ResponseStatusException(
//            HttpStatus.NOT_FOUND,
//            "task with id '$id' not found"
//        )
//    }

    private enum class SearchCriterion { description, completed }
}
