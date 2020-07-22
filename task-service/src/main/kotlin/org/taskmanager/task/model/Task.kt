package org.taskmanager.task.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Task(
        @Id
        val id: Long? = null,
        val description: String,
        var completed: Boolean
)
