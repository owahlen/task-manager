package org.taskmanager.task.repository

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.model.ItemTag


@SpringBootTest
@DirtiesContext
class ItemTagRepositoryIntegrationTest(@Autowired val itemTagRepository: ItemTagRepository) {

    @Test
    fun `test that item-tags can be loaded from db using findAllByItemId`() {
        runBlocking {
            // when
            val nItemTags = itemTagRepository.count()
            // then
            assertThat(nItemTags).isGreaterThan(0)
            // when
            val existingItemTag = itemTagRepository.findAllByItemId(4).first()
            // then
            assertThat(existingItemTag).isNotNull()
            assertThat(existingItemTag.id).isNotNull()
            assertThat(existingItemTag.itemId).isEqualTo(4)
            assertThat(existingItemTag.tagId).isEqualTo(2)
        }
    }

    @Test
    fun `test create and update of item-tags`() {
        runBlocking {
            // when
            var existingItem = itemTagRepository.save(ItemTag(itemId = 1, tagId = 3))
            // then
            assertThat(existingItem).isNotNull()
            assertThat(existingItem.id).isNotNull()

            // when
            existingItem.tagId = 4
            existingItem = itemTagRepository.save(existingItem)
            // then
            assertThat(existingItem).isNotNull()
        }
    }

    @Test
    fun `test findAllByItemId and deleteAllByItemId`() {
        runBlocking {
            // setup
            val testItemId = 3L
            // when
            val nItemTagsBeforeDeletion = itemTagRepository.findAllByItemId(testItemId).count()
            // then
            assertThat(nItemTagsBeforeDeletion).isGreaterThan(0)
            // when
            itemTagRepository.deleteAllByItemId(testItemId)
            // then
            val nItemTagsAfterDeletion = itemTagRepository.findAllByItemId(testItemId).count()
            assertThat(nItemTagsAfterDeletion).isEqualTo(0)
        }
    }

    @Test
    fun `test findAllByTagId and deleteAllByTagId`() {
        runBlocking {
            // setup
            val testTagId = 3L
            // when
            val nItemTagsBeforeDeletion = itemTagRepository.findAllByTagId(testTagId).count()
            // then
            assertThat(nItemTagsBeforeDeletion).isGreaterThan(0)
            // when
            itemTagRepository.deleteAllByTagId(testTagId)
            // then
            val nItemTagsAfterDeletion = itemTagRepository.findAllByTagId(testTagId).count()
            assertThat(nItemTagsAfterDeletion).isEqualTo(0)
        }
    }

}

