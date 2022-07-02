package org.taskmanager.task.kafka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

enum class OperationType {
    CREATE, UPDATE, DELETE, ACTION
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class KeycloakAdminEvent(
    val id: String,
    val time: Long,
    val realmId: String,
    val authDetails: KeycloakAuthDetails,
    val resourceType: String,
    val operationType: OperationType,
    val resourcePath: String,
    // The representation is in JSON format but wrapped into a String
    @JsonDeserialize(using = FromStringJsonDeserializer::class)
    val representation: Map<String, Any>,
    val error: String?,
)

