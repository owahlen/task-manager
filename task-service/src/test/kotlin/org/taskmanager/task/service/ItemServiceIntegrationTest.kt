package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.`as`
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.model.Tag


@SpringBootTest
@DirtiesContext
class ItemServiceIntegrationTest(@Autowired val itemService: ItemService) {

    @Test
    fun `test findAll is ordered by lastModifiedDate`() {
        runBlocking {
            // when
            val items = itemService.findAll().toList()
            // then
            assertThat(items.count()).isGreaterThan(2)
            val sortedItems = items.sortedBy { it.lastModifiedDate }
            assertThat(items).isEqualTo(sortedItems)
        }
    }

    @Test
    fun `test create Item with description and status`() {
        runBlocking {
            // setup
            val testItem = Item(description = "test with description and status", status = ItemStatus.TODO)
            // when
            val savedTestItem = itemService.create(testItem)
            // then
            assertThat(savedTestItem.id).isNotNull()
            assertThat(savedTestItem.createdDate).isNotNull()
            assertThat(savedTestItem.lastModifiedDate).isNotNull()
        }
    }

    @Test
    fun `test create Item with tags`() {
        runBlocking {
            // setup
            val testItem = Item(description = "test with tags", status = ItemStatus.TODO)
            testItem.tags = listOf(Tag(name = "test tag 1"), Tag(name = "test tag 2"))
            // when
            val savedTestItem = itemService.create(testItem)
            // then
            assertThat(savedTestItem.id).isNotNull()
            assertThat(savedTestItem.tags).isNotEmpty
            assertThat(savedTestItem.createdDate).isNotNull()
            assertThat(savedTestItem.lastModifiedDate).isNotNull()
        }
    }
}

