package org.taskmanager.task.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.stereotype.Repository
import org.taskmanager.task.model.Tag

@Repository
interface TagRepository : CoroutineSortingRepository<Tag, Long> {

    fun findAllBy(pageable: Pageable): Flow<Tag>

    @Query("SELECT t.* FROM tag t INNER JOIN item_tag it on t.id = it.tag_id WHERE it.item_id = :item_id ORDER BY t.name")
    fun findTagsByItemId(itemId: Long): Flow<Tag>

}
