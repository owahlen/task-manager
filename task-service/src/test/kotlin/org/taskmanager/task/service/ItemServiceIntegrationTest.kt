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
import org.taskmanager.task.api.resource.ItemCreateResource


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
            val itemResources = itemService.findAllBy(pageable).toList()
            // then
            assertThat(itemResources.count()).isGreaterThan(2)
            val sortedItems = itemResources.sortedBy { it.lastModifiedDate }
            assertThat(itemResources).isEqualTo(sortedItems)
        }
    }

    @Test
    fun `test create Item with description and status`() {
        runBlocking {
            // setup
            val testItemCreateResource = ItemCreateResource(description = "test with description and status")
            // when
            val savedTestItem = itemService.create(testItemCreateResource)
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
            val testItemCreateResource = ItemCreateResource(description = "test with tags")
            testItemCreateResource.tagIds = setOf(1,2)
            // when
            val savedTestItemCreateResource = itemService.create(testItemCreateResource)
            // then
            assertThat(savedTestItemCreateResource.id).isNotNull()
            assertThat(savedTestItemCreateResource.tags?.size).isEqualTo(2)
            assertThat(savedTestItemCreateResource.createdDate).isNotNull()
            assertThat(savedTestItemCreateResource.lastModifiedDate).isNotNull()
        }
    }
}

