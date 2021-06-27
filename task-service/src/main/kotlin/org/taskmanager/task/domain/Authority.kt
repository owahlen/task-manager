package org.taskmanager.task.domain

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Table
data class Authority(
    @Id
    @field:NotNull
    @field:Size(max = 50)
    var name: String? = null
) : Serializable, Persistable<String> {
    override fun getId() = name
    override fun isNew() = true
}
