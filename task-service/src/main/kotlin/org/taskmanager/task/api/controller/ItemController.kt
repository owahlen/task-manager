package org.taskmanager.task.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import org.taskmanager.task.api.resource.ItemCreateResource
import org.taskmanager.task.api.resource.ItemPatchResource
import org.taskmanager.task.api.resource.ItemResource
import org.taskmanager.task.api.resource.ItemUpdateResource
import org.taskmanager.task.mapper.toItem
import org.taskmanager.task.mapper.toItemResource
import org.taskmanager.task.model.Item
import org.taskmanager.task.service.ItemService
import javax.validation.Valid

@RestController
@RequestMapping("/item")
class ItemController(private val itemService: ItemService) {

    @Operation(
        summary = "Get page of items",
        responses = [ApiResponse(responseCode = "200", description = "got page of items")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun getAllItems(
        @PageableDefault(value = 100, sort = ["lastModifiedDate", "description"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<ItemResource> {
        return itemService.findAllBy(pageable).map(Item::toItemResource)
    }

    @Operation(
        summary = "Get a specific item",
        responses = [ApiResponse(responseCode = "200", description = "got item by id"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "item not found")]
    )
    @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun getItemById(@PathVariable id: Long): ItemResource {
        return itemService.getById(id).toItemResource()
    }

    @Operation(
        summary = "Create an item",
        responses = [ApiResponse(responseCode = "200", description = "item created"),
            ApiResponse(responseCode = "400", description = "bad parameter")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun createItem(@Valid @RequestBody itemCreateResource: ItemCreateResource): ItemResource {
        return itemService.create(itemCreateResource.toItem()).toItemResource()
    }

    @Operation(
        summary = "Update an item",
        responses = [ApiResponse(responseCode = "200", description = "item updated"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "item not found")]
    )
    @PutMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun updateItem(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody itemUpdateResource: ItemUpdateResource
    ): ItemResource {
        return itemService.update(itemUpdateResource.toItem(id, version)).toItemResource()
    }

    @Operation(
        summary = "Patch an item",
        responses = [ApiResponse(responseCode = "200", description = "item patched"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "item not found")]
    )
    @PatchMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun patchItem(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody itemPatchResource: ItemPatchResource
    ): ItemResource {
        val item = itemService.getById(id, version, true)
        return itemService.update(itemPatchResource.toItem(item)).toItemResource()
    }

    @Operation(
        summary = "Delete an item",
        responses = [ApiResponse(responseCode = "200", description = "item deleted"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "item not found")]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteItem(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        itemService.deleteById(id, version)
    }
}
