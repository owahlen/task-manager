package org.taskmanager.task.configuration

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.repository.UserRepository
import org.taskmanager.task.service.KeycloakUserService
import org.taskmanager.task.service.UserService

@Component
class ContextInitialization(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val keycloakUserService: KeycloakUserService
    ) {

    private val log = LoggerFactory.getLogger(ContextInitialization::class.java)

    @EventListener
    fun onApplicationEvent(event: ContextRefreshedEvent) {
        runBlocking {
            if (userRepository.findByEmail("admin")==null) {
                tryCreateAdminUser()
            }
        }
    }

    private suspend fun tryCreateAdminUser() {
        // todo: this should be done in a distributed lock
        log.info("creating admin user...")
        val userCreateResource = UserCreateResource(
            email="admin",
            password = "password",
            firstName = "",
            lastName = ""
        )
        val userResource = userService.create(userCreateResource)
        val uuid = userResource.uuid!!
        keycloakUserService.assignToGroup(uuid,"task-manager-admins")
        log.info("admin user created with uuid '${userResource.uuid}'")
    }
}