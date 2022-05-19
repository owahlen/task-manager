package org.taskmanager.task.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import javax.validation.constraints.*

@Table
data class Item(
    @Id
    val id: Long? = null,

    @Version
    val version: Long? = null,

    @NotBlank
    @Size(max = 4000)
    var description: String? = null,

    @NotNull
    var status: ItemStatus = ItemStatus.TODO,

    var assigneeId: Long? = null,

    @Transient
    @Value("null")
    var assignee: Person? = null,

    @Transient
    @Value("null")
    var tags: List<Tag>? = null,

    @CreatedDate
    val createdDate: LocalDateTime? = null,

    @LastModifiedDate
    val lastModifiedDate: LocalDateTime? = null
)
