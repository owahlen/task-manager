package org.taskmanager.task.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Table("users")
data class User(
    @Id
    val id: Long? = null,

    @Version
    val version: Long? = null,

    @NotBlank
    @Size(max = 36)
    var userId: String? = null,

    @NotBlank
    @Size(max = 256)
    @Email
    var email: String? = null,

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