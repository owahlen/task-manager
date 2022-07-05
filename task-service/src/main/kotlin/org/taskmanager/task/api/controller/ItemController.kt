package org.taskmanager.task.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.taskmanager.task.api.dto.ItemCreateDto
import org.taskmanager.task.api.dto.ItemPatchDto
import org.taskmanager.task.api.dto.ItemDto
import org.taskmanager.task.api.dto.ItemUpdateDto
import org.taskmanager.task.service.ItemService
import javax.validation.Valid

@RestController
@RequestMapping("/item")
class ItemController(private val itemService: ItemService) {

    @Operation(
        summary = "Get page of items",
        responses = [
            ApiResponse(responseCode = "200", description = "got page of items"),
            ApiResponse(responseCode = "403", description = "insufficient privileges")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun getAllItems(
        @PageableDefault(value = 100, sort = ["lastModifiedDate", "description"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<ItemDto> {
        return itemService.findAllBy(pageable)
    }

    @Operation(
        summary = "Get a specific item",
        responses = [
            ApiResponse(responseCode = "200", description = "got item by id"),
            ApiResponse(responseCode = "400", description = "bad parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "item not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun getItemById(@PathVariable id: Long): ItemDto {
        return itemService.getById(id)
    }

    @Operation(
        summary = "Create an item",
        responses = [
            ApiResponse(responseCode = "200", description = "item created"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun createItem(@Valid @RequestBody itemCreateDto: ItemCreateDto): ItemDto {
        return itemService.create(itemCreateDto)
    }

    @Operation(
        summary = "Update an item",
        responses = [
            ApiResponse(responseCode = "200", description = "item updated"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "item not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PutMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun updateItem(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody itemUpdateDto: ItemUpdateDto
    ): ItemDto {
        return itemService.update(id, version, itemUpdateDto)
    }

    @Operation(
        summary = "Patch an item",
        responses = [
            ApiResponse(responseCode = "200", description = "item patched"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "item not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PatchMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun patchItem(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody itemPatchDto: ItemPatchDto
    ): ItemDto {
        return itemService.patch(id, version, itemPatchDto)
    }

    @Operation(
        summary = "Delete an item",
        responses = [
            ApiResponse(responseCode = "200", description = "item deleted"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "item not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun deleteItem(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        itemService.delete(id, version)
    }
}
