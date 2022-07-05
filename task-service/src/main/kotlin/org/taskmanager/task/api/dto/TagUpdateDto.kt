package org.taskmanager.task.api.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class TagUpdateDto(
    @field:NotBlank
    @field:Size(max = 100)
    var name: String? = null,
)
