package org.taskmanager.task.repository

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.test.annotation.DirtiesContext
import org.taskmanager.task.IntegrationTest
import org.taskmanager.task.model.User


@IntegrationTest
@DirtiesContext
class UserRepositoryIntegrationTest(@Autowired val userRepository: UserRepository) {

    @Test
    fun `test that users can be loaded from db`() {
        runBlocking {
            // when
            val nUsers = userRepository.count()
            // then
            assertThat(nUsers).isGreaterThan(0)
            // when
            val existingUser = userRepository.findById(1)
            // then
            assertThat(existingUser).isNotNull()
            assertThat(existingUser!!.id).isEqualTo(1)
            assertThat(existingUser.version).isEqualTo(1)
            assertThat(existingUser.firstName).isEqualTo("Richard")
            assertThat(existingUser.lastName).isEqualTo("Countin")
            assertThat(existingUser.createdDate).isNotNull()
            assertThat(existingUser.lastModifiedDate).isNotNull()
        }
    }

    @Test
    fun `test creation, update and optimistic locking for users`() {
        runBlocking {
            // when
            var existingUser = userRepository.save(User(firstName = "John", lastName = "Doe"))
            // then
            assertThat(existingUser).isNotNull()
            assertThat(existingUser.id).isNotNull()
            assertThat(existingUser.version).isEqualTo(0)

            // when
            existingUser.lastName = "Walker"
            existingUser = userRepository.save(existingUser)
            // then
            assertThat(existingUser).isNotNull()
            assertThat(existingUser.version).isEqualTo(1)

            // setup
            val userToUpdate = User(id = existingUser.id, version = 0, lastName = "Dalton")
            // When / Then
            assertThatThrownBy {
                runBlocking {
                    userRepository.save(userToUpdate)
                }
            }.isInstanceOf(OptimisticLockingFailureException::class.java)
        }
    }

    @Test
    fun `test findAllBy pageable returns page of users`() {
        runBlocking {
            // setup
            val sort = Sort.by(Order.by("firstName"), Order.by("lastName"))
            val pageable = PageRequest.of(0, 100, sort)
            // when
            val users = userRepository.findAllBy(pageable).toList()
            // then
            assertThat(users.count()).isGreaterThan(2)
            val sortedUsers = users.sortedWith(compareBy(User::firstName, User::lastName))
            assertThat(users).isEqualTo(sortedUsers)
        }
    }
}
