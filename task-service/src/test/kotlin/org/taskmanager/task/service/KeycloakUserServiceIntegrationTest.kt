package org.taskmanager.task.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.api.resource.UserCreateResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.exception.UnexpectedUserVersionException
import org.taskmanager.task.mapper.toUser
import org.taskmanager.task.model.User


@IntegrationTest
class KeycloakUserServiceIntegrationTest(@Autowired val keycloakUserService: KeycloakUserService) {

    @Test
    fun `test create update delete user`() {
        runBlocking {
            // setup
            val email = "roger.donald@test.org"
            val password = "password"
            // when
            val uuid = keycloakUserService.create(email, password)
            // then
            assertThat(uuid).isNotBlank
            assertThat(uuid).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")
        }
    }

}
