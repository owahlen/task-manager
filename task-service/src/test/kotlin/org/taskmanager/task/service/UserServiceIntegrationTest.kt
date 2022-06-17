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
import org.taskmanager.task.api.resource.UserResource
import org.taskmanager.task.api.resource.UserUpdateResource
import org.taskmanager.task.exception.UserNotFoundException
import org.taskmanager.task.exception.UnexpectedUserVersionException


@IntegrationTest
@DirtiesContext
class UserServiceIntegrationTest(@Autowired val userService: UserService) {

    @Test
    fun `test findAllBy pageable returns page of users`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("firstName"), Order.by("lastName"), Order.by("email"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val userResources = userService.findAllBy(pageable).toList()
            // then
            assertThat(userResources.count()).isGreaterThan(2)
            val sortedUsers = userResources.sortedWith(
                compareBy(UserResource::firstName, UserResource::lastName, UserResource::email)
            )
            assertThat(userResources).isEqualTo(sortedUsers)
        }
    }

    @Test
    fun `test getById returns user or throws UserNotFoundException`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000001"
            // when
            val existingUserResource = userService.getByUuid(uuid)
            // then
            assertThat(existingUserResource).isNotNull()
            assertThat(existingUserResource.uuid).isEqualTo(uuid)

            // when / then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid("fffffff-ffff-ffff-ffff-ffffffffffff")
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

    @Test
    fun `test getById with wrong version throws UnexpectedUserVersionException`() {
        runBlocking {
            // when / then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid("00000000-0000-0000-0000-000000000001", -1)
                }
            }.isInstanceOf(UnexpectedUserVersionException::class.java)
        }
    }

    @Test
    fun `test create user`() {
        runBlocking {
            // setup
            val userCreateResource = UserCreateResource(
                "john.walker@test.org",
                "password",
                "John",
                "Walker"
            )
            // when
            val savedUser = userService.create(userCreateResource)
            // then
            assertThat(savedUser).isNotNull
            assertThat(savedUser.uuid).isNotNull
            assertThat(savedUser.version).isNotNull
            assertThat(savedUser.email).isEqualTo(userCreateResource.email)
            // todo: test that the password is stored
            assertThat(savedUser.firstName).isEqualTo(userCreateResource.firstName)
            assertThat(savedUser.lastName).isEqualTo(userCreateResource.lastName)
            assertThat(savedUser.createdDate).isNotNull
            assertThat(savedUser.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test update user without password change`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000002"
            val userUpdateResource = UserUpdateResource(
                "john.walker@test.org",
                null,
                "John",
                "Walker"
            )
            // when
            val updatedUser = userService.update(uuid, null, userUpdateResource)
            // then
            assertThat(updatedUser).isNotNull
            assertThat(updatedUser.uuid).isEqualTo(uuid)
            assertThat(updatedUser.version).isNotNull
            assertThat(updatedUser.email).isEqualTo(userUpdateResource.email)
            // todo: test that the password is not changed
            assertThat(updatedUser.firstName).isEqualTo(userUpdateResource.firstName)
            assertThat(updatedUser.lastName).isEqualTo(userUpdateResource.lastName)
            assertThat(updatedUser.createdDate).isNotNull
            assertThat(updatedUser.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test update user with password change`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000002"
            val userUpdateResource = UserUpdateResource(
                "till.harper@test.org",
                "password",
                "Till",
                "Harper"
            )
            // when
            val updatedUser = userService.update(uuid, null, userUpdateResource)
            // then
            assertThat(updatedUser).isNotNull
            assertThat(updatedUser.uuid).isEqualTo(uuid)
            assertThat(updatedUser.version).isNotNull
            assertThat(updatedUser.email).isEqualTo(userUpdateResource.email)
            // todo: test that the password is changed
            assertThat(updatedUser.firstName).isEqualTo(userUpdateResource.firstName)
            assertThat(updatedUser.lastName).isEqualTo(userUpdateResource.lastName)
            assertThat(updatedUser.createdDate).isNotNull
            assertThat(updatedUser.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test delete user`() {
        runBlocking {
            // setup
            val uuid = "00000000-0000-0000-0000-000000000003"
            val userResource = userService.getByUuid(uuid)
            assertThat(userResource).isNotNull
            // when
            userService.delete(uuid)
            // then
            assertThatThrownBy {
                runBlocking {
                    userService.getByUuid(uuid)
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

}
