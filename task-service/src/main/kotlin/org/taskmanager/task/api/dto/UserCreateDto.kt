package org.taskmanager.task.api.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class UserCreateDto(
    @field:NotBlank
    @field:Size(max = 256)
    var userId: String? = null,

    @field:NotBlank
    @field:Size(max = 256)
    @field:Email
    var email: String? = null,

    @field:NotBlank
    @field:Size(max = 100)
    var firstName: String? = null,

    @field:NotBlank
    @field:Size(max = 100)
    var lastName: String? = null,
)
