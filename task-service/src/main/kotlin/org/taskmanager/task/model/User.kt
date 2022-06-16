package org.taskmanager.task.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Table("users")
data class User(
    @Id
    val id: Long? = null,

    @Version
    val version: Long? = null,

    @NotBlank
    @Size(max = 100)
    var firstName: String? = null,

    @NotBlank
    @Size(max = 100)
    var lastName: String? = null,

    @CreatedDate
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    val lastModifiedDate: LocalDateTime? = null
)