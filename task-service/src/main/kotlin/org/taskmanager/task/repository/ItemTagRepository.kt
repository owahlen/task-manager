package org.taskmanager.task.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.taskmanager.task.model.ItemTag


@Repository
interface ItemTagRepository : CoroutineCrudRepository<ItemTag, Long> {

    fun findAllByItemId(itemId: Long): Flow<ItemTag>

    fun findAllByTagId(tagId: Long): Flow<ItemTag>

    suspend fun deleteAllByItemId(itemId: Long)

    suspend fun deleteAllByTagId(tagId: Long)

}
