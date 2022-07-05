package org.taskmanager.task.configuration.keycloak

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.taskmanager.task.repository.UserRepository
import java.util.*

class FakeKeycloakUserStore(
    private val groupName: String
) {
    // only one group is supported
    val group = FakeKeycloakGroup(id = UUID.randomUUID().toString(), name = groupName)

    // map userId to FakeKeycloakUser
    val users: MutableMap<String, FakeKeycloakUser> = mutableMapOf()

    // list of all the userIds of the users in the group
    val groupUsers: MutableSet<String> = mutableSetOf()

    fun initializeFakeKeycloakUserStore(userRepository: UserRepository) {
        users.clear()
        groupUsers.clear()
        runBlocking {
            val users = userRepository.findAll().toList()
            users.forEach {
                this@FakeKeycloakUserStore.users[it.userId!!] = FakeKeycloakUser(
                    userId = it.userId!!,
                    username = it.email!!,
                    email = it.email,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    password = it.email!!,
                    isEnabled = true
                )
                groupUsers.add(it.userId!!)
            }
        }

    }
}
