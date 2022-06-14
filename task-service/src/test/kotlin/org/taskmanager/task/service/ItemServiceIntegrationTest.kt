package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemStatus
import org.taskmanager.task.model.Tag


@IntegrationTest
@DirtiesContext
class ItemServiceIntegrationTest(@Autowired val itemService: ItemService) {

    @Test
    fun `test findAllBy pageable returns page of items`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("lastModifiedDate"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val items = itemService.findAllBy(pageable).toList()
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
            testItem.tags = listOf(Tag(id = 1), Tag(id = 2))
            // when
            val savedTestItem = itemService.create(testItem)
            // then
            assertThat(savedTestItem.id).isNotNull()
            assertThat(savedTestItem.tags?.size).isEqualTo(2)
            assertThat(savedTestItem.createdDate).isNotNull()
            assertThat(savedTestItem.lastModifiedDate).isNotNull()
        }
    }
}

