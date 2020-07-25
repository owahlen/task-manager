package org.taskmanager.task.repository

import org.taskmanager.task.model.Task
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : CoroutineCrudRepository<Task, Long> {

    fun findByDescriptionContainingIgnoreCase(description: String): Flow<Task>

    @Query("SELECT t.* FROM task t WHERE t.completed = :completed")
    fun findByCompleted(completed: Boolean): Flow<Task>

}
