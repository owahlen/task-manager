package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.dto.TagCreateDto
import org.taskmanager.task.api.dto.TagDto
import org.taskmanager.task.api.dto.TagUpdateDto
import org.taskmanager.task.exception.TagNotFoundException
import org.taskmanager.task.exception.UnexpectedTagVersionException


@IntegrationTest
@DirtiesContext
class TagServiceIntegrationTest(@Autowired val tagService: TagService) {

    @Test
    fun `test findAllBy pageable returns page of tags`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("name"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val tagResources = tagService.findAllBy(pageable).toList()
            // then
            assertThat(tagResources.count()).isGreaterThan(2)
            val sortedTags = tagResources.sortedBy(TagDto::name)
            assertThat(tagResources).isEqualTo(sortedTags)
        }
    }

    @Test
    fun `test getById returns tag or throws TagNotFoundException`() {
        runBlocking {
            // when
            val existingTagResource = tagService.getById(1)
            // then
            assertThat(existingTagResource).isNotNull()
            assertThat(existingTagResource.id).isEqualTo(1)

            // when / then
            assertThatThrownBy {
                runBlocking {
                    tagService.getById(-1)
                }
            }.isInstanceOf(TagNotFoundException::class.java)
        }
    }

    @Test
    fun `test getById with wrong version throws UnexpectedTagVersionException`() {
        runBlocking {
            // when / then
            assertThatThrownBy {
                runBlocking {
                    tagService.getById(1, -1)
                }
            }.isInstanceOf(UnexpectedTagVersionException::class.java)
        }
    }

    @Test
    fun `test create tag`() {
        runBlocking {
            // setup
            val tagCreateDto = TagCreateDto("Weather")
            // when
            val savedTag = tagService.create(tagCreateDto)
            // then
            assertThat(savedTag).isNotNull
            assertThat(savedTag.id).isNotNull
            assertThat(savedTag.version).isNotNull
            assertThat(savedTag.name).isEqualTo(tagCreateDto.name)
            assertThat(savedTag.createdDate).isNotNull
            assertThat(savedTag.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test update tag`() {
        runBlocking {
            // setup
            val tagId = 2L
            val tagUpdateDto = TagUpdateDto("Weather")
            // when
            val updatedTag = tagService.update(tagId, null, tagUpdateDto)
            // then
            assertThat(updatedTag).isNotNull
            assertThat(updatedTag.id).isEqualTo(tagId)
            assertThat(updatedTag.version).isNotNull
            assertThat(updatedTag.name).isEqualTo(tagUpdateDto.name)
            assertThat(updatedTag.createdDate).isNotNull
            assertThat(updatedTag.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test delete tag`() {
        runBlocking {
            // setup
            val tagId = 3L
            val tagResource = tagService.getById(tagId)
            assertThat(tagResource).isNotNull
            // when
            tagService.delete(tagId)
            // then
            assertThatThrownBy {
                runBlocking {
                    tagService.getById(tagId)
                }
            }.isInstanceOf(TagNotFoundException::class.java)
        }
    }
}
