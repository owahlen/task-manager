package org.taskmanager.task.service

import org.taskmanager.task.model.Task
import org.taskmanager.task.model.TaskDTO
import org.taskmanager.task.model.toModel
import org.springframework.stereotype.Service
import org.taskmanager.task.repository.TaskRepository

@Service
class TaskService(private val taskRepository: TaskRepository) {

    fun findAll() = taskRepository.findAll()
    suspend fun findById(id: Long) = taskRepository.findById(id)
    fun findByDescription(description: String) = taskRepository.findByDescriptionContainingIgnoreCase(description)
    fun findByCompleted(completed: Boolean) = taskRepository.findByCompleted(completed)
    suspend fun create(task: TaskDTO) = taskRepository.save(task.toModel())

    suspend fun update(id: Long, taskDTO: TaskDTO): Task? {
        if(findById(id)==null) return null
        return taskRepository.save(taskDTO.toModel(withId = id))
    }

    suspend fun delete(id: Long): Boolean {
        if(findById(id)==null) return false
        taskRepository.deleteById(id)
        return true
    }

}
