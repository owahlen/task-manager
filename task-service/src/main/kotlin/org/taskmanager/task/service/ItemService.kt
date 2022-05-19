package org.taskmanager.task.service

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taskmanager.task.exception.ItemNotFoundException
import org.taskmanager.task.exception.UnexpectedItemVersionException
import org.taskmanager.task.model.Item
import org.taskmanager.task.model.ItemTag
import org.taskmanager.task.model.Tag
import org.taskmanager.task.repository.ItemRepository
import org.taskmanager.task.repository.ItemTagRepository
import org.taskmanager.task.repository.PersonRepository
import org.taskmanager.task.repository.TagRepository


@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val personRepository: PersonRepository,
    private val itemTagRepository: ItemTagRepository,
    private val tagRepository: TagRepository
) {

    // Note that the name of the fields to be sorted on are the DB field names
    private val DEFAULT_SORT: Sort = Sort.by(Sort.Order.by("lastModifiedDate"))

    /**
     * Find all items
     * @return Find all items with the related objects loaded
     */
    fun findAll() = itemRepository.findAll(DEFAULT_SORT).map(::loadRelations)

    /**
     * Create a new item
     * @param item Item to be created
     *
     * @return the saved item without the related entities
     */
    @Transactional
    suspend fun create(item: Item): Item {
        if (item.id != null || item.version != null) {
            throw IllegalArgumentException("When creating an item, the id and the version must be null")
        }
        return itemRepository.save(item).also { savedItem ->
            item.tags?.map { tag -> ItemTag(savedItem.id!!, tag.id) }
                ?.let { itemTags -> itemTagRepository.saveAll(itemTags) }
        }
    }

    /**
     * Update an Item
     * @param itemToSave item to be saved
     * @return the saved item without the related entities
     */
    @Transactional
    suspend fun update(itemToSave: Item): Item {
        if (itemToSave.id == null || itemToSave.version == null) {
            throw IllegalArgumentException("When updating an item, the id and the version must be provided")
        }
        // verify the item with id and version exists.
        getById(itemToSave.id, itemToSave.version, false)

        // find the existing item-tag mappings
        val currentItemTags = itemTagRepository.findAllByItemId(itemToSave.id).toList()

        // Remove and add the links to the tags
        // As R2DBC does not support embedded IDs, the ItemTag entity has a technical key
        // We can't just replace all ItemTags, we need to generate the proper insert/delete statements
        val existingTagIds = currentItemTags.map(ItemTag::tagId)
        val tagIdsToSave = itemToSave.tags?.map(Tag::id) ?: listOf()
        // Item Tags to be deleted
        val removedItemTags = currentItemTags.filter { !tagIdsToSave.contains(it.id) }
        // Item Tags to be inserted
        val addedItemTags = tagIdsToSave.filter { !existingTagIds.contains(it) }.map { ItemTag(itemToSave.id, it) }
        itemTagRepository.deleteAll(removedItemTags)
        itemTagRepository.saveAll(addedItemTags)
        // Save the item
        return itemRepository.save(itemToSave)
    }

    @Transactional
    suspend fun deleteById(id: Long, version: Long?) {
        val item = getById(id, version, false)
        itemTagRepository.deleteAllByItemId(id)
        itemRepository.delete(item)
    }

    /**
     * Get an item
     *
     * @param id            identifier of the item
     * @param version       expected version to be retrieved
     * @param loadRelations true if the related objects must also be retrieved
     *
     * @return the item
     */
    suspend fun getById(id: Long, version: Long? = null, loadRelations: Boolean = false): Item {
        val item = itemRepository.findById(id)?:throw ItemNotFoundException(id)
        if (version != null && version != item.version) {
            // Optimistic locking: pre-check
            throw UnexpectedItemVersionException(version, item.version!!)
        }
        // Load the related objects, if requested
        return item.also {
            if (loadRelations) loadRelations(it)
        }
    }

    /**
     * Load the objects related to an item
     * @param item Item
     * @return The items with the loaded related objects (assignee, tags)
     */
    private suspend fun loadRelations(item: Item): Item {

        // Load the tags
        item.tags = tagRepository.findTagsByItemId(item.id!!).toList()

        // Load the assignee (if set)
        val assigneeId = item.assigneeId
        if (assigneeId != null) item.assignee = personRepository.findById(assigneeId)

        return item
    }

}