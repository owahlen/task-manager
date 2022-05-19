package org.taskmanager.task.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import org.taskmanager.task.model.Person

@Repository
interface PersonRepository : CoroutineSortingRepository<Person, Long> {
    fun findAllBy(pageable: Pageable): Flow<Person>
}

