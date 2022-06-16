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
import org.taskmanager.task.api.resource.TagCreateResource
import org.taskmanager.task.api.resource.TagPatchResource
import org.taskmanager.task.api.resource.TagResource
import org.taskmanager.task.api.resource.TagUpdateResource
import org.taskmanager.task.mapper.toTag
import org.taskmanager.task.mapper.toTagResource
import org.taskmanager.task.model.Tag
import org.taskmanager.task.service.TagService
import javax.validation.Valid

@RestController
@RequestMapping("/tag")
class TagController(private val tagService: TagService) {

    @Operation(
        summary = "Get page of tags",
        responses = [ApiResponse(responseCode = "200", description = "got page of tags")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun getAllUsers(
        @PageableDefault(value = 100, sort = ["name"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<TagResource> {
        return tagService.findAllBy(pageable).map(Tag::toTagResource)
    }

    @Operation(
        summary = "Get a specific tag",
        responses = [ApiResponse(responseCode = "200", description = "got tag by id"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "tag not found")]
    )
    @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun getTagById(@PathVariable id: Long): TagResource {
        return tagService.getById(id).toTagResource()
    }

    @Operation(
        summary = "Create a tag",
        responses = [ApiResponse(responseCode = "200", description = "tag created"),
            ApiResponse(responseCode = "400", description = "bad parameter")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun createTag(@Valid @RequestBody tagCreateResource: TagCreateResource): TagResource {
        return tagService.create(tagCreateResource.toTag()).toTagResource()
    }

    @Operation(
        summary = "Update a tag",
        responses = [ApiResponse(responseCode = "200", description = "tag updated"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "tag not found")]
    )
    @PutMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun updateTag(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody tagUpdateResource: TagUpdateResource
    ): TagResource {
        return tagService.update(tagUpdateResource.toTag(id, version)).toTagResource()
    }

    @Operation(
        summary = "Patch a tag",
        responses = [ApiResponse(responseCode = "200", description = "tag patched"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "tag not found")]
    )
    @PatchMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun patchTag(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody tagPatchResource: TagPatchResource
    ): TagResource {
        val tag = tagService.getById(id, version)
        return tagService.update(tagPatchResource.toTag(tag)).toTagResource()
    }

    @Operation(
        summary = "Delete a tag",
        responses = [ApiResponse(responseCode = "200", description = "tag deleted"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "tag not found")]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteTag(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        tagService.deleteById(id, version)
    }
}
