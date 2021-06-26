package org.taskmanager.task.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import javax.validation.constraints.*

@Table
data class Task(
        @Id
        var id: Long? = null,

        @Column("description")
        @get: NotNull(message = "must not be null")
        var description: String? = null,

        @Column("completed")
        @get: NotNull(message = "must not be null")
        var completed: Boolean? = null
)
