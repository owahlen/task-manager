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
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserPatchResource
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.mapper.toUser
import org.taskmanager.task.mapper.toUserResource
import org.taskmanager.task.model.User
import org.taskmanager.task.service.UserService
import javax.validation.Valid

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @Operation(
        summary = "Get page of users",
        responses = [ApiResponse(responseCode = "200", description = "got page of users")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun getAllUsers(
        @PageableDefault(value = 100, sort = ["firstName", "lastName"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<UserResource> {
        return userService.findAllBy(pageable).map(User::toUserResource)
    }

    @Operation(
        summary = "Get a specific user",
        responses = [ApiResponse(responseCode = "200", description = "got user by id"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun getUserById(@PathVariable id: Long): UserResource {
        return userService.getById(id).toUserResource()
    }

    @Operation(
        summary = "Create a user",
        responses = [ApiResponse(responseCode = "200", description = "user created"),
            ApiResponse(responseCode = "400", description = "bad parameter")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun createUser(@Valid @RequestBody userCreateResource: UserCreateResource): UserResource {
        return userService.create(userCreateResource.toUser()).toUserResource()
    }

    @Operation(
        summary = "Update a user",
        responses = [ApiResponse(responseCode = "200", description = "user updated"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @PutMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun updateUser(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody userUpdateResource: UserUpdateResource
    ): UserResource {
        return userService.update(userUpdateResource.toUser(id, version)).toUserResource()
    }

    @Operation(
        summary = "Patch a user",
        responses = [ApiResponse(responseCode = "200", description = "user patched"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @PatchMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun patchUser(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody userPatchResource: UserPatchResource
    ): UserResource {
        val user = userService.getById(id, version)
        return userService.update(userPatchResource.toUser(user)).toUserResource()
    }

    @Operation(
        summary = "Delete a user",
        responses = [ApiResponse(responseCode = "200", description = "user deleted"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteUser(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        userService.deleteById(id, version)
    }
}
