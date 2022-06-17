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
        @PageableDefault(value = 100, sort = ["firstName", "lastName", "email"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<UserResource> {
        return userService.findAllBy(pageable)
    }

    @Operation(
        summary = "Get a specific user",
        responses = [ApiResponse(responseCode = "200", description = "got user by id"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @GetMapping("/{uuid}", produces = [APPLICATION_JSON_VALUE])
    suspend fun getUserById(@PathVariable uuid: String): UserResource {
        return userService.getByUuid(uuid)
    }

    @Operation(
        summary = "Create a user",
        responses = [ApiResponse(responseCode = "200", description = "user created"),
            ApiResponse(responseCode = "400", description = "bad parameter")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun createUser(@Valid @RequestBody userCreateResource: UserCreateResource): UserResource {
        return userService.create(userCreateResource)
    }

    @Operation(
        summary = "Update a user",
        responses = [ApiResponse(responseCode = "200", description = "user updated"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @PutMapping("/{uuid}", produces = [APPLICATION_JSON_VALUE])
    suspend fun updateUser(
        @PathVariable uuid: String,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody userUpdateResource: UserUpdateResource
    ): UserResource {
        return userService.update(uuid, version, userUpdateResource)
    }

    @Operation(
        summary = "Patch a user",
        responses = [ApiResponse(responseCode = "200", description = "user patched"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @PatchMapping("/{uuid}", produces = [APPLICATION_JSON_VALUE])
    suspend fun patchUser(
        @PathVariable uuid: String,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody userPatchResource: UserPatchResource
    ): UserResource {
        return userService.patch(uuid, version, userPatchResource)
    }

    @Operation(
        summary = "Delete a user",
        responses = [ApiResponse(responseCode = "200", description = "user deleted"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "user not found")]
    )
    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteUser(
        @PathVariable uuid: String,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        userService.delete(uuid, version)
    }
}
