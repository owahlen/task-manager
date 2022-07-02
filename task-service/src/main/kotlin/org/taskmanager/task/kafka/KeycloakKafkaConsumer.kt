package org.taskmanager.task.kafka

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.taskmanager.task.service.UserService

@Component
class KeycloakKafkaConsumer(private val userService: UserService) {

    private val log = LoggerFactory.getLogger(KeycloakKafkaConsumer::class.java)

    fun handleKeycloakEvent(event: KeycloakEvent) {
        log.trace("Consumed event: $event")
        userService.synchronizeUserFromKeycloak(event.userId)

    }

    fun handleKeycloakAdminEvent(event: KeycloakAdminEvent) {
        log.trace("Consumed admin event: $event")
        if (event.resourceType == "USER") {
            val userId = event.resourcePath.split("/").last()
            userService.synchronizeUserFromKeycloak(userId)
        }
    }

}
