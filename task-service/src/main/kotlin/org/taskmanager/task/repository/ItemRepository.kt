package org.taskmanager.task.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import org.taskmanager.task.model.Item

@Repository
interface ItemRepository : CoroutineSortingRepository<Item, Long> {
    fun findByAssigneeId(assigneeId: Long): Flow<Item>
    suspend fun deleteByAssigneeId(assigneeId: Long)
}
