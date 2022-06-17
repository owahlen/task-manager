package org.taskmanager.task.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import org.taskmanager.task.model.User

@Repository
interface UserRepository : CoroutineSortingRepository<User, Long> {
    fun findAllBy(pageable: Pageable): Flow<User>
    suspend fun findByUuid(uuid: String): User?
}

