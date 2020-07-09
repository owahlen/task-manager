package com.example.rest.task

import com.example.rest.model.Task
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TaskRepository : ReactiveCrudRepository<Task, Long> {

    @Query("SELECT t.* FROM task t WHERE t.completed = :completed")
    fun findByCompleted(completed: Boolean): Flux<Task>

}
