package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.api.resource.TagCreateResource
import org.taskmanager.task.api.resource.TagUpdateResource
import org.taskmanager.task.exception.TagNotFoundException
import org.taskmanager.task.exception.UnexpectedTagVersionException
import org.taskmanager.task.mapper.toTag
import org.taskmanager.task.model.Tag


@SpringBootTest
@DirtiesContext
class TagServiceIntegrationTest(@Autowired val tagService: TagService) {

    @Test
    fun `test findAllBy pageable returns page of tags`() {
        runBlocking {
            // setup
            val sort = Sort.by(Sort.Order.by("name"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val tags = tagService.findAllBy(pageable).toList()
            // then
            assertThat(tags.count()).isGreaterThan(2)
            val sortedTags = tags.sortedBy(Tag::name)
            assertThat(tags).isEqualTo(sortedTags)
        }
    }

    @Test
    fun `test getById returns tag or throws TagNotFoundException`() {
        runBlocking {
            // when
            val existingTag = tagService.getById(1)
            // then
            assertThat(existingTag).isNotNull()
            assertThat(existingTag.id).isEqualTo(1)

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
            val tag = TagCreateResource("Weather").toTag()
            // when
            val savedTag = tagService.create(tag)
            // then
            assertThat(savedTag).isNotNull
            assertThat(savedTag.id).isNotNull
            assertThat(savedTag.version).isNotNull
            assertThat(savedTag.name).isEqualTo(tag.name)
            assertThat(savedTag.createdDate).isNotNull
            assertThat(savedTag.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test update tag`() {
        runBlocking {
            // setup
            val tag = TagUpdateResource("Weather").toTag(2, null)
            // when
            val updatedTag = tagService.update(tag)
            // then
            assertThat(updatedTag).isNotNull
            assertThat(updatedTag.id).isEqualTo(2)
            assertThat(updatedTag.version).isNotNull
            assertThat(updatedTag.name).isEqualTo(tag.name)
            assertThat(updatedTag.createdDate).isNotNull
            assertThat(updatedTag.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test delete tag`() {
        runBlocking {
            // setup
            val tag = tagService.getById(3)
            assertThat(tag).isNotNull
            // when
            tagService.deleteById(3)
            // then
            assertThatThrownBy {
                runBlocking {
                    tagService.getById(3)
                }
            }.isInstanceOf(TagNotFoundException::class.java)
        }
    }
}
