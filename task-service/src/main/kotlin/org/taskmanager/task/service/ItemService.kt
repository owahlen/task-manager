package org.taskmanager.task.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.exception.ItemNotFoundException
import org.taskmanager.task.exception.UnexpectedItemVersionException
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemTag
import org.taskmanager.task.model.Tag
import org.taskmanager.task.repository.ItemRepository
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.UserRepository
import org.taskmanager.task.repository.TagRepository


@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository,
    private val itemTagRepository: ItemTagRepository,
    private val tagRepository: TagRepository
) {

    /**
     * Get a page of items
     * @param pageable page definition
     * @return Page of items
     */
    suspend fun findAllBy(pageable: Pageable): Page<Item> {
        val dataPage = itemRepository.findAllBy(pageable).map(::populateRelations).toList()
        val total = itemRepository.count()
        return PageImpl(dataPage, pageable, total)
    }

    /**
     * Get an item with version check
     * @param id            id of the item
     * @param version       expected version to be retrieved
     * @param loadRelations true if the related objects must also be retrieved
     * @return the currently stored item
     */
    suspend fun getById(id: Long, version: Long? = null, loadRelations: Boolean = false): Item {
        val item = itemRepository.findById(id) ?: throw ItemNotFoundException(id)
        if (version != null && version != item.version) {
            // Optimistic locking: pre-check
            throw UnexpectedItemVersionException(version, item.version!!)
        }
        // Load the related objects, if requested
        return item.also {
            if (loadRelations) populateRelations(it)
        }
    }

    /**
     * Create a new item
     * @param item item to be created
     * @return the created item without the related entities
     */
    @Transactional
    suspend fun create(item: Item): Item {
        if (item.id != null || item.version != null) {
            throw IllegalArgumentException("When creating an item, the id and the version must be null")
        }
        val savedItem = itemRepository.save(item)
        item.tags?.map { tag ->
            ItemTag(itemId = savedItem.id!!, tagId = tag.id)
        }?.forEach {
            // Note: saveAll does not work with R2DBC therefore each itemTag is saved, individually
            itemTagRepository.save(it)
        }
        return savedItem.also {
            populateRelations(savedItem)
        }
    }

    /**
     * Update an item with version check
     * @param item item to be saved
     * @return the saved item without the related entities
     */
    @Transactional
    suspend fun update(item: Item): Item {
        if (item.id == null) {
            throw IllegalArgumentException("When updating an item, the id must be provided")
        }
        // verify that the item with id exists and if version!=null then check that it matches
        val storedItem = getById(item.id, item.version, false)
        val itemToSave = item.copy(version = storedItem.version, createdDate = storedItem.createdDate)

        // find the existing item-tag mappings
        val currentItemTags = itemTagRepository.findAllByItemId(itemToSave.id!!).toList()

        // Remove and add the links to the tags
        // As R2DBC does not support embedded IDs, the ItemTag entity has a technical key
        // We can't just replace all ItemTags, we need to generate the proper insert/delete statements
        val existingTagIds = currentItemTags.map(ItemTag::tagId)
        val tagIdsToSave = itemToSave.tags?.map(Tag::id) ?: listOf()
        // Item Tags to be deleted
        val removedItemTags = currentItemTags.filter {
            !tagIdsToSave.contains(it.tagId)
        }
        // Item Tags to be inserted
        val addedItemTags = tagIdsToSave.filter { !existingTagIds.contains(it) }.map {
            ItemTag(itemId = itemToSave.id, tagId = it)
        }
        itemTagRepository.deleteAll(removedItemTags)
        addedItemTags.forEach {
            // Note: saveAll does not work with R2DBC therefore each itemTag is saved, individually
            itemTagRepository.save(it)
        }
        // Save the item
        return itemRepository.save(itemToSave).also {
            populateRelations(it)
        }
    }

    /**
     * Delete an item with version check
     * This method transitively deletes item-tags of the item
     * @param id id of the item to be deleted
     * @param version if not null check that version matches the version of the currently stored item
     */
    @Transactional
    suspend fun deleteById(id: Long, version: Long? = null) {
        // check that item with this id exists
        val item = getById(id, version, false)
        itemTagRepository.deleteAllByItemId(id)
        itemRepository.delete(item)
    }

    /**
     * Populate the tags and assignee related to an item
     * @param item Item
     * @return The items with the loaded related objects (assignee, tags)
     */
    private suspend fun populateRelations(item: Item): Item {
        // Load the tags
        item.tags = tagRepository.findTagsByItemId(item.id!!).toList()

        // Load the assignee (if set)
        val assigneeId = item.assigneeId
        if (assigneeId != null) item.assignee = userRepository.findById(assigneeId)

        return item
    }

}
