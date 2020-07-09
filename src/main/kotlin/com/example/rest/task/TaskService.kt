package com.example.rest.task

import com.example.rest.model.Task
import com.example.rest.model.TaskDTO
import com.example.rest.model.toModel
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    suspend fun findAll() = taskRepository.findAll().asFlow()

    suspend fun findById(id: Long) = taskRepository.findById(id).awaitFirstOrNull()

    suspend fun findByCompleted(completed: Boolean) = taskRepository.findByCompleted(completed).asFlow()

    suspend fun create(task: TaskDTO) = taskRepository.save(task.toModel()).awaitFirstOrNull()

    suspend fun update(id: Long, task: TaskDTO): Task? {
        val existingTask = findById(id)
        return if (existingTask != null) taskRepository.save(task.toModel(withId = id)).awaitFirstOrNull() else null
    }

    suspend fun delete(id: Long): Boolean {
        val existingTask = findById(id)
        return if (existingTask != null) {
            taskRepository.delete(existingTask).awaitFirstOrNull()
            true
        } else false
    }

}
