package org.taskmanager.user.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("application_user")
data class User(
        @Id
        val id: Long? = null,
        val email: String,
        var password: String
)
