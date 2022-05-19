package org.taskmanager.task.service

import org.springframework.stereotype.Service
import org.taskmanager.task.repository.TagRepository

@Service
class TaskServiceOld(private val taskRepository: TagRepository) {

//    fun findAll() = taskRepository.findAll().map { it.toTaskDTO() }
//    suspend fun findById(id: Long) = taskRepository.findById(id)?.toTaskDTO()
//    fun findByDescription(description: String) =
//        taskRepository.findByDescriptionContainingIgnoreCase(description).map { it.toTaskDTO() }
//
//    fun findByCompleted(completed: Boolean) = taskRepository.findByCompleted(completed).map { it.toTaskDTO() }
//    suspend fun create(taskDTO: TaskDTO) = taskRepository.save(taskDTO.toTask()).toTaskDTO()
//
//    @Transactional
//    suspend fun update(taskDTO: TaskDTO): TaskDTO? {
//        val id = taskDTO.id!!
//        if (findById(id) == null) return null
//        return taskRepository.save(taskDTO.copy(id = id).toTask()).toTaskDTO()
//    }
//
//    @Transactional
//    suspend fun delete(id: Long): Boolean {
//        if (findById(id) == null) return false
//        taskRepository.deleteById(id)
//        return true
//    }

}
