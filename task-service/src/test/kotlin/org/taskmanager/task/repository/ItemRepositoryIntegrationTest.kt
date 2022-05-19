package org.taskmanager.task.repository

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


@SpringBootTest
@DirtiesContext
class ItemRepositoryIntegrationTest(@Autowired val itemRepository: ItemRepository) {

    @Test
    fun `test that items can be loaded from db`() {
        runBlocking {
            // when
            val nItems = itemRepository.count()
            // then
            assertThat(nItems).isGreaterThan(0)
            // when
            val existingItem = itemRepository.findById(1)
            // then
            assertThat(existingItem).isNotNull()
            assertThat(existingItem!!.id).isEqualTo(1)
            assertThat(existingItem.version).isEqualTo(1)
            assertThat(existingItem.description).isEqualTo("Flight to JNB")
            assertThat(existingItem.status).isEqualTo(ItemStatus.TODO)
            assertThat(existingItem.assigneeId).isEqualTo(1)
            assertThat(existingItem.assignee).isNull()
            assertThat(existingItem.tags).isNull()
            assertThat(existingItem.createdDate).isNotNull()
            assertThat(existingItem.lastModifiedDate).isNotNull()
        }
    }

    @Test
    fun `test creation, update and optimistic locking for items`() {
        runBlocking {
            // when
            var existingItem = itemRepository.save(Item(description = "Walk the dog", status = ItemStatus.TODO))
            // then
            assertThat(existingItem).isNotNull()
            assertThat(existingItem.id).isNotNull()
            assertThat(existingItem.version).isEqualTo(0)

            // when
            existingItem.description = "Walk the dog in the park"
            existingItem = itemRepository.save(existingItem)
            // then
            assertThat(existingItem).isNotNull()
            assertThat(existingItem.version).isEqualTo(1)

            // setup
            val itemToUpdate = Item(id = existingItem.id, version = 0, description = "Walk the dog by the river")
            // When / Then
            assertThatThrownBy {
                runBlocking {
                    itemRepository.save(itemToUpdate)
                }
            }.isInstanceOf(OptimisticLockingFailureException::class.java)

        }
    }
}

