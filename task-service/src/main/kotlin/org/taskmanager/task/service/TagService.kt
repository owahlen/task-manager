package org.taskmanager.task.service

import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.exception.TagNotFoundException
import org.taskmanager.task.exception.UnexpectedTagVersionException
import org.taskmanager.task.model.Tag
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.TagRepository


@Service
class TagService(
    private val tagRepository: TagRepository,
    private val itemTagRepository: ItemTagRepository
) {

    /**
     * Get a page of tags
     * @param pageable page definition
     * @return flow of tags
     */
    suspend fun findAllBy(pageable: Pageable): Page<Tag> {
        val dataPage = tagRepository.findAllBy(pageable).toList()
        val total = tagRepository.count()
        return PageImpl(dataPage, pageable, total)
    }

    /**
     * Get a tag with version check
     * @param id id of the tag
     * @param version if version is not null check with currently stored user
     * @return the currently stored tag
     */
    suspend fun getById(id: Long, version: Long? = null): Tag {
        val tag = tagRepository.findById(id) ?: throw TagNotFoundException(id)
        if (version != null && version != tag.version) {
            // Optimistic locking: pre-check
            throw UnexpectedTagVersionException(version, tag.version!!)
        }
        return tag
    }

    /**
     * Create a new tag
     * @param tag tag to be created
     * @return the created tag
     */
    @Transactional
    suspend fun create(tag: Tag): Tag {
        if (tag.id != null || tag.version != null) {
            throw IllegalArgumentException("When creating a tag, the id and the version must be null")
        }
        return tagRepository.save(tag)
    }

    /**
     * Update a tag with version check
     * @param tag tag to be updated; if tag's version is not null check with currently stored tag
     * @return the updated tag
     */
    @Transactional
    suspend fun update(tag: Tag): Tag {
        if (tag.id == null) {
            throw IllegalArgumentException("When updating a tag, the id must be provided")
        }
        // verify that the tag with id exists and if version!=null then check that it matches
        val storedTag = getById(tag.id, tag.version)
        val tagToSave = tag.copy(version = storedTag.version, createdDate = storedTag.createdDate)
        // Save the tag
        return tagRepository.save(tagToSave)
    }

    /**
     * Delete a tag with version check
     * This method transitively deletes item-tags of the tag
     * @param id id of the tag to be deleted
     * @param version if not null check that version matches the version of the currently stored tag
     */
    @Transactional
    suspend fun deleteById(id: Long, version: Long? = null) {
        // check that tag with this id exists
        val tag = getById(id, version)
        itemTagRepository.deleteAllByTagId(id)
        tagRepository.delete(tag)
    }

}