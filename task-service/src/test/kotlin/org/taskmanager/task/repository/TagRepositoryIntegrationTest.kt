package org.taskmanager.task.repository

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.model.Tag


@IntegrationTest
@DirtiesContext
class TagRepositoryIntegrationTest(@Autowired val tagRepository: TagRepository) {

    @Test
    fun `test that tags can be loaded from db`() {
        runBlocking {
            // when
            val nTags = tagRepository.count()
            // then
            assertThat(nTags).isGreaterThan(0)
            // when
            val existingTag = tagRepository.findById(1)
            // then
            assertThat(existingTag).isNotNull()
            assertThat(existingTag!!.id).isEqualTo(1)
            assertThat(existingTag.version).isEqualTo(1)
            assertThat(existingTag.name).isEqualTo("Work")
            assertThat(existingTag.createdDate).isNotNull()
            assertThat(existingTag.lastModifiedDate).isNotNull()
        }
    }

    @Test
    fun `test creation, update and optimistic locking for tags`() {
        runBlocking {
            // when
            var existingTag = tagRepository.save(Tag(name = "School"))
            // then
            assertThat(existingTag).isNotNull()
            assertThat(existingTag.id).isNotNull()
            assertThat(existingTag.version).isEqualTo(0)

            // when
            existingTag.name = "University"
            existingTag = tagRepository.save(existingTag)
            // then
            assertThat(existingTag).isNotNull()
            assertThat(existingTag.version).isEqualTo(1)

            // setup
            val tagToUpdate = Tag(id = existingTag.id, version = 0, name = "Education")
            // When / Then
            assertThatThrownBy {
                runBlocking {
                    tagRepository.save(tagToUpdate)
                }
            }.isInstanceOf(OptimisticLockingFailureException::class.java)

        }
    }

    @Test
    fun `test findTagsByItemId`() {
        runBlocking {
            // when
            val tags = tagRepository.findTagsByItemId(1).toList()
            // then
            assertThat(tags.size).isGreaterThanOrEqualTo(2)
        }
    }
}

