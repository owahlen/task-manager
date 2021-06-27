package org.taskmanager.task.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

const val LOGIN_REGEX: String =
    "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"

@Table
data class User(
    @Id
    var id: Long? = null,

    @field:NotNull
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    var login: String? = null,

    @JsonIgnore
    @field:NotNull
    @field:Size(min = 60, max = 60)
    @Column("password_hash")
    var password: String? = null,

    @field:Size(max = 50)
    @Column("first_name")
    var firstName: String? = null,

    @field:Size(max = 50)
    @Column("last_name")
    var lastName: String? = null,

    @field:Email
    @field:Size(min = 5, max = 254)
    var email: String? = null,

    @field:NotNull
    var activated: Boolean? = false,

    @Column
    @ReadOnlyProperty
    @field:Size(max = 50)
    @field:NotNull
    @JsonIgnore
    var createdBy: String? = null,

    @Column
    @CreatedDate
    @ReadOnlyProperty
    @JsonIgnore
    var createdDate: Instant? = Instant.now(),

    @Column
    @field:Size(max = 50)
    @JsonIgnore
    var lastModifiedBy: String? = null,

    @LastModifiedDate
    @Column
    @JsonIgnore
    var lastModifiedDate: Instant? = Instant.now(),

    @JsonIgnore
    @Transient
    var authorities: MutableSet<Authority> = mutableSetOf(),
) : Serializable
