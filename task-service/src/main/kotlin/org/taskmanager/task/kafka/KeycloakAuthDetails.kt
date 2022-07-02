package org.taskmanager.task.kafka

data class KeycloakAuthDetails(
    val realmId: String,
    val clientId: String,
    val userId: String,
    val ipAddress: String
)