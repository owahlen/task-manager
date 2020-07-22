package org.taskmanager.user.repository

import kotlinx.coroutines.flow.Flow
import org.taskmanager.user.model.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {

    suspend fun findByEmailIgnoreCase(email: String): Flow<User>

}
