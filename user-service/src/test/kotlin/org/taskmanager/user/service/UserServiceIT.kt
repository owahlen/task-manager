package org.taskmanager.user.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.taskmanager.user.configuration.DatabaseConfiguration
import org.taskmanager.user.configuration.UserDetailsConfiguration
import org.taskmanager.user.model.User
import org.taskmanager.user.model.UserDTO
import org.taskmanager.user.model.toModel
import org.taskmanager.user.repository.UserRepository
import org.taskmanager.user.toDto

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DataR2dbcTest
@Import(UserDetailsConfiguration::class, DatabaseConfiguration::class)
internal class UserServiceIT {

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    private val testUserIds = ArrayList<Long>()

    @BeforeAll
    fun beforeAll() {
        userService = UserService(userRepository, passwordEncoder)
        runBlocking {
            initDatabase().map { it.id!! }.toList(testUserIds)
        }
    }

    @AfterEach
    fun afterEach() {
        runBlocking {
            val cleanupUsers = userRepository.findAll().filter { !testUserIds.contains(it.id) }
            userRepository.deleteAll(cleanupUsers)
        }
    }

    @Test
    @Order(1)
    fun `findAll returns values`() {
        runBlocking {
            // when
            val resp = userService.findAll()
            // then
            assertThat(resp.count()).isEqualTo(3)
        }
    }

    @Test
    fun `findById returns a value`() {
        runBlocking {
            // setup
            val user1Id = testUserIds.first()
            // when
            val resp = userService.findById(user1Id)
            // then
            assertThat(resp).isNotNull()
            assertThat(resp?.email).isEqualTo("User1")
        }
    }

    @Test
    fun `findById returns a null if value does not exists`() {
        runBlocking {
            // when
            val resp = userService.findById(999)
            // then
            assertThat(resp).isNull()
        }
    }

    @Test
    fun `findByUsername returns a value`() {
        runBlocking {
            // setup
            val userDTO = UserDTO("NewUser", "Password")
            val newUser = userRepository.save(userDTO.toModel(passwordEncoder))
            assertThat(newUser).isNotNull()
            // when
            val resp = userService.findByEmail("NewUser")
            // then
            assertThat(resp.count()).isEqualTo(1)
            resp.map {
                assertThat(it.email).isEqualTo("NewUser")
            }
        }
    }

    @Test
    fun `findByUsername returns an empty flow if no user is found`() {
        runBlocking {
            // when
            val resp = userService.findByEmail("nonExistingUser")
            // then
            assertThat(resp.count()).isEqualTo(0)
        }
    }

    @Test
    fun `create creates a user`() {
        runBlocking {
            // setup
            val userDTO = UserDTO("NewUser", "Password")
            // when
            val resp = userService.create(userDTO)
            // then
            assertThat(resp.id).isNotNull()
            val dbUser = userRepository.findById(resp.id!!)
            assertThat(dbUser).isNotNull()
            assertThat(dbUser!!.email).isEqualTo("NewUser")
            assertThat(dbUser.password).isNotBlank()
            assertThat(dbUser.password).matches("\\{.+}.+") // DelegatingPasswordEncoder is used
        }
    }

    @Test
    fun `update updates a user`() {
        runBlocking {
            // setup
            val newUser = createUser(UserDTO("NewUser", "OldPassword"))
            val newUserId = newUser.id!!
            // when
            val resp = userService.update(newUserId, newUser.toDto(password = "NewPassword"))
            // then
            assertThat(resp).isNotNull()
            assertThat(resp?.id).isNotNull()
            val dbUser = userRepository.findById(newUserId)
            assertThat(dbUser).isNotNull()
            assertThat(dbUser!!.email).isEqualTo("NewUser")
            assertThat(dbUser.password).isNotBlank()
            assertThat(dbUser.password).matches("\\{.+}.+") // DelegatingPasswordEncoder is used

        }
    }

    @Test
    fun `update nonexistent user returns null`() {
        runBlocking {
            // setup
            val nonexistentUser = UserDTO("Nonexistent", "password")
            // when
            val resp = userService.update(999, nonexistentUser)
            // then
            assertThat(resp).isNull()
        }
    }

    @Test
    fun `delete deletes existing user returns true`() {
        runBlocking {
            // setup
            val newUser = createUser(UserDTO("User to delete", "Password"))
            val newUserId = newUser.id!!
            // when
            val resp = userService.delete(newUserId)
            // then
            assertThat(resp).isTrue()
            val dbUser = userRepository.findById(newUserId)
            assertThat(dbUser).isNull()
        }
    }

    @Test
    fun `delete deletes nonexistent user returns false`() {
        runBlocking {
            // when
            val resp = userService.delete(9999)
            // then
            assertThat(resp).isFalse()
        }
    }

    private suspend fun initDatabase(): Flow<User> {
        // delete all users
        userRepository.deleteAll()
        // return Flow of new user creations
        val initData = flowOf(
                User(null, "User1", "Password1"),
                User(null, "User2", "Password2"),
                User(null, "User3", "Password3")
        )
        return userRepository.saveAll(initData)
    }

    private suspend fun createUser(userDTO: UserDTO): User {
        val newUser = userRepository.save(userDTO.toModel(passwordEncoder))
        assertThat(newUser).isNotNull()
        assertThat(newUser.id).isNotNull()
        return newUser
    }
}
