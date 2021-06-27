package org.taskmanager.task.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.taskmanager.task.domain.User
import java.awt.print.Pageable

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {

    suspend fun findOneByLogin(login: String): User?
    suspend fun findOneByEmailIgnoreCase(email: String): User?
    fun findAllByActivatedIsTrue(pageable: Pageable): Flow<User>
}
