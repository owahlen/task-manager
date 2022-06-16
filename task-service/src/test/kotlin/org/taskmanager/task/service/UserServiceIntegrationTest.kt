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
@DirtiesContext
class UserServiceIntegrationTest(@Autowired val userService: UserService) {

    @Test
    fun `test findAllBy pageable returns page of users`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("firstName"), Order.by("lastName"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val users = userService.findAllBy(pageable).toList()
            // then
            assertThat(users.count()).isGreaterThan(2)
            val sortedUsers = users.sortedWith(compareBy(User::firstName, User::lastName))
            assertThat(users).isEqualTo(sortedUsers)
        }
    }

    @Test
    fun `test getById returns user or throws UserNotFoundException`() {
        runBlocking {
            // when
            val existingUser = userService.getById(1)
            // then
            assertThat(existingUser).isNotNull()
            assertThat(existingUser.id).isEqualTo(1)

            // when / then
            assertThatThrownBy {
                runBlocking {
                    userService.getById(-1)
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
                    userService.getById(1, -1)
                }
            }.isInstanceOf(UnexpectedUserVersionException::class.java)
        }
    }

    @Test
    fun `test create user`() {
        runBlocking {
            // setup
            val user = UserCreateResource("John", "Walker").toUser()
            // when
            val savedUser = userService.create(user)
            // then
            assertThat(savedUser).isNotNull
            assertThat(savedUser.id).isNotNull
            assertThat(savedUser.version).isNotNull
            assertThat(savedUser.firstName).isEqualTo(user.firstName)
            assertThat(savedUser.lastName).isEqualTo(user.lastName)
            assertThat(savedUser.createdDate).isNotNull
            assertThat(savedUser.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test update user`() {
        runBlocking {
            // setup
            val user = UserUpdateResource("John", "Walker").toUser(2, null)
            // when
            val updatedUser = userService.update(user)
            // then
            assertThat(updatedUser).isNotNull
            assertThat(updatedUser.id).isEqualTo(2)
            assertThat(updatedUser.version).isNotNull
            assertThat(updatedUser.firstName).isEqualTo(user.firstName)
            assertThat(updatedUser.lastName).isEqualTo(user.lastName)
            assertThat(updatedUser.createdDate).isNotNull
            assertThat(updatedUser.lastModifiedDate).isNotNull
        }
    }

    @Test
    fun `test delete user`() {
        runBlocking {
            // setup
            val user = userService.getById(3)
            assertThat(user).isNotNull
            // when
            userService.deleteById(3)
            // then
            assertThatThrownBy {
                runBlocking {
                    userService.getById(3)
                }
            }.isInstanceOf(UserNotFoundException::class.java)
        }
    }

}

