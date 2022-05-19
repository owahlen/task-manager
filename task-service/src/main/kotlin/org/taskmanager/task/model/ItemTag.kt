package org.taskmanager.task.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import javax.validation.constraints.NotNull

@Table
data class ItemTag(
    @Id
    val id: Long? = null,

    @NotNull
    var itemId: Long? = null,

    @NotNull
    var tagId: Long? = null
)