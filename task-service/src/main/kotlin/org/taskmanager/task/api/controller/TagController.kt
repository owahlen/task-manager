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
import org.taskmanager.task.api.dto.TagCreateDto
import org.taskmanager.task.api.dto.TagPatchDto
import org.taskmanager.task.api.dto.TagDto
import org.taskmanager.task.api.dto.TagUpdateDto
import org.taskmanager.task.service.TagService
import javax.validation.Valid

@RestController
@RequestMapping("/tag")
class TagController(private val tagService: TagService) {

    @Operation(
        summary = "Get page of tags",
        responses = [
            ApiResponse(responseCode = "200", description = "got page of tags"),
            ApiResponse(responseCode = "403", description = "insufficient privileges")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun getAllTags(
        @PageableDefault(value = 100, sort = ["name"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<TagDto> {
        return tagService.findAllBy(pageable)
    }

    @Operation(
        summary = "Get a specific tag",
        responses = [
            ApiResponse(responseCode = "200", description = "got tag by id"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "tag not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun getTagById(@PathVariable id: Long): TagDto {
        return tagService.getById(id)
    }

    @Operation(
        summary = "Create a tag",
        responses = [
            ApiResponse(responseCode = "200", description = "tag created"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun createTag(@Valid @RequestBody tagCreateDto: TagCreateDto): TagDto {
        return tagService.create(tagCreateDto)
    }

    @Operation(
        summary = "Update a tag",
        responses = [
            ApiResponse(responseCode = "200", description = "tag updated"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "tag not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PutMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun updateTag(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody tagUpdateDto: TagUpdateDto
    ): TagDto {
        return tagService.update(id, version, tagUpdateDto)
    }

    @Operation(
        summary = "Patch a tag",
        responses = [
            ApiResponse(responseCode = "200", description = "tag patched"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "tag not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PatchMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun patchTag(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody tagPatchDto: TagPatchDto
    ): TagDto {
        return tagService.patch(id, version, tagPatchDto)
    }

    @Operation(
        summary = "Delete a tag",
        responses = [
            ApiResponse(responseCode = "200", description = "tag deleted"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "tag not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    suspend fun deleteTag(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        tagService.delete(id, version)
    }
}
