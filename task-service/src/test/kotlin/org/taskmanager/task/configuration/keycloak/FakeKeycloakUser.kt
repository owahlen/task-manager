package org.taskmanager.task.configuration.keycloak

import org.keycloak.representations.idm.UserRepresentation

data class FakeKeycloakUser(
    var uuid: String,
    var username: String,
    var email: String?,
    var firstName: String?,
    var lastName: String?,
    var password: String,
    var isEnabled: Boolean
)

fun FakeKeycloakUser.toUserRepresentation(): UserRepresentation {
    val userRepresentation = UserRepresentation()
    userRepresentation.id = this.uuid
    userRepresentation.username = this.username
    userRepresentation.email = this.email
    userRepresentation.firstName = this.firstName
    userRepresentation.lastName = this.lastName
    userRepresentation.isEnabled = this.isEnabled
    return userRepresentation
}
