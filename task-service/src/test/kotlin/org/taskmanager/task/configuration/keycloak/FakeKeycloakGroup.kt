package org.taskmanager.task.configuration.keycloak

import org.keycloak.representations.idm.GroupRepresentation

data class FakeKeycloakGroup(
    var id: String,
    var name: String
)

fun FakeKeycloakGroup.toGroupRepresentation(): GroupRepresentation {
    val groupRepresentation = GroupRepresentation()
    groupRepresentation.id = this.id
    groupRepresentation.name = this.name
    return groupRepresentation
}