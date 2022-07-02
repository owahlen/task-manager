package org.taskmanager.task.kafka

data class KeycloakEvent(
    val id: String,
    val time: Long,
    val type: String,
    val realmId: String,
    val clientId: String,
    val userId: String,
    val sessionId: String? = null,
    val ipAddress: String,
    val error: String? = null,
    val details: Map<String, String> = mapOf()
)
