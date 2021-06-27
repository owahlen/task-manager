package org.taskmanager.task.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.taskmanager.task.domain.Authority

interface AuthorityRepository : CoroutineCrudRepository<Authority, String>
