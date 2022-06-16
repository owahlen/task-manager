package org.taskmanager.task.api.resource

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class UserCreateResource(
    @field:NotBlank
    @field:Size(max = 100)
    var firstName: String? = null,

    @field:NotBlank
    @field:Size(max = 100)
    var lastName: String? = null,
)
