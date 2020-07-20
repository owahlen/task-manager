package org.example.taskmanager.task

import org.example.taskmanager.model.Task
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : CoroutineCrudRepository<Task, Long> {

    @Query("SELECT t.* FROM task t WHERE t.completed = :completed")
    fun findByCompleted(completed: Boolean): Flow<Task>

}
