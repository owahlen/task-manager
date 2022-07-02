package org.taskmanager.task.kafka

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.taskmanager.task.service.KeycloakUserService

@Component
class KeycloakKafkaConsumer(private val keycloakUserService: KeycloakUserService) {

    private val log = LoggerFactory.getLogger(KeycloakKafkaConsumer::class.java)

    fun handleKeycloakEvent(event: KeycloakEvent) {
        log.trace("Consumed event: $event")
        synchronizeUser(event.userId)

    }

    fun handleKeycloakAdminEvent(event: KeycloakAdminEvent) {
        log.trace("Consumed admin event: $event")
        if(event.resourceType == "USER") {
            val userId = event.resourcePath.split("/").last()
            synchronizeUser(userId)
        }
    }

    private fun synchronizeUser(userId: String) {
        val userRepresentation = keycloakUserService.findById(userId)
        // todo: delete, create, or update the user
    }

}
