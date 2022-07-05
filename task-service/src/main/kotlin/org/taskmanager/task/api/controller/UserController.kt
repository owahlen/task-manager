package org.taskmanager.task.api.controller


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.taskmanager.task.api.dto.UserDto
import org.taskmanager.task.service.UserService

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @Operation(
        summary = "Get page of users",
        responses = [
            ApiResponse(responseCode = "200", description = "got page of users"),
            ApiResponse(responseCode = "403", description = "insufficient privileges")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    suspend fun getAllUsers(
        @PageableDefault(value = 100, sort = ["firstName", "lastName", "email"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<UserDto> {
        return userService.findAllBy(pageable)
    }

    @Operation(
        summary = "Get a specific user",
        responses = [
            ApiResponse(responseCode = "200", description = "got user by id"),
            ApiResponse(responseCode = "400", description = "bad request parameters"),
            ApiResponse(responseCode = "403", description = "insufficient privileges"),
            ApiResponse(responseCode = "404", description = "user not found")],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/{userId}", produces = [APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    suspend fun getUserById(@PathVariable userId: String): UserDto {
        return userService.getByUserId(userId)
    }

}
