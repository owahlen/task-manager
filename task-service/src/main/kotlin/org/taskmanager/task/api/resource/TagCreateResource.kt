package org.taskmanager.task.api.resource

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class TagCreateResource(
    @field:NotBlank
    @field:Size(max = 100)
    var name: String? = null,
)
