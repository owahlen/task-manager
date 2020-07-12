package com.example.rest.task

import com.example.rest.model.Task
import com.example.rest.model.TaskDTO
import com.example.rest.model.toModel
import org.springframework.stereotype.Service

@Service
class TaskService(private val taskRepository: TaskRepository) {

    suspend fun findAll() = taskRepository.findAll()
    suspend fun findById(id: Long) = taskRepository.findById(id)
    suspend fun findByCompleted(completed: Boolean) = taskRepository.findByCompleted(completed)
    suspend fun create(task: TaskDTO) = taskRepository.save(task.toModel())

    suspend fun update(id: Long, task: TaskDTO): Task? {
        if(findById(id)==null) return null
        return taskRepository.save(task.toModel(withId = id))
    }

    suspend fun delete(id: Long): Boolean {
        if(findById(id)==null) return false
        taskRepository.deleteById(id)
        return true
    }

}
