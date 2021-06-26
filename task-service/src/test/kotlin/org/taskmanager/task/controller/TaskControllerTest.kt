package org.taskmanager.task.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.body
import org.springframework.test.web.reactive.server.expectBody
import org.taskmanager.task.configuration.ApiSecurityConfiguration
import org.taskmanager.task.createTaskDTO
import org.taskmanager.task.service.TaskService
import org.taskmanager.task.service.UserService
import org.taskmanager.task.service.dto.TaskDTO

@WebFluxTest(TaskController::class)
@Import(ApiSecurityConfiguration::class)
internal class TaskControllerTest {

    private val controllerRequestMapping = "/api/v1"

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var taskService: TaskService

    @MockkBean
    private lateinit var userService: UserService

    @Test
    fun `GET task returns array of all tasks`() {
        // setup
        val taskDTOs = arrayOf(createTaskDTO(id = 1), createTaskDTO(id = 2))
        coEvery { taskService.findAll() } returns taskDTOs.asFlow()
        // when
        webTestClient.get()
            .uri("$controllerRequestMapping/task")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isOk
            .expectBody<Array<TaskDTO>>()
            .isEqualTo(taskDTOs)
    }

    @Test
    fun `GET task returns empty array if no tasks exist`() {
        // setup
        coEvery { taskService.findAll() } returns emptyFlow()
        // when
        webTestClient.get()
            .uri("$controllerRequestMapping/task")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            //then
            .expectStatus().isOk
            .expectBody<Array<TaskDTO>>()
            .isEqualTo(arrayOf())
    }

    @Test
    fun `GET task-search returns filtered tasks`() {
        // setup
        val taskDTOs = arrayOf(createTaskDTO(id = 1, completed = true), createTaskDTO(id = 2, completed = true))
        coEvery { taskService.findByCompleted(true) } returns taskDTOs.asFlow()
        // when
        webTestClient.get()
            .uri("$controllerRequestMapping/task/search?completed=true")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isOk
            .expectBody<Array<TaskDTO>>()
            .isEqualTo(taskDTOs)
    }

    @Test
    fun `GET task-id returns existing task`() {
        // setup
        val taskDTO = createTaskDTO(id = 1)
        coEvery { taskService.findById(1) } returns taskDTO
        // when
        webTestClient.get()
            .uri("$controllerRequestMapping/task/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            //then
            .expectStatus().isOk
            .expectBody<TaskDTO>()
            .isEqualTo(taskDTO)
    }

    @Test
    fun `GET task-id returns NOT_FOUND for nonexistent task`() {
        // setup
        coEvery { taskService.findById(999) } returns null
        // when
        webTestClient.get()
            .uri("$controllerRequestMapping/task/999")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            //then
            .expectStatus().isNotFound
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/999",
                    HttpStatus.NOT_FOUND
                )
            }
    }

    @Test
    fun `GET task-id returns BAD_REQUEST if id is not a positive integer`() {
        // when
        webTestClient.get()
            .uri("$controllerRequestMapping/task/ABC")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            //then
            .expectStatus().isBadRequest
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/ABC",
                    HttpStatus.BAD_REQUEST
                )
            }
    }

    @Test
    fun `POST task creates a task`() {
        // setup
        val taskDTO = createTaskDTO()
        coEvery { taskService.create(any()) } answers {
            firstArg<TaskDTO>().copy(id = 999)
        }
        // when
        webTestClient.post()
            .uri("$controllerRequestMapping/task")
            .body<TaskDTO>(CompletableDeferred(taskDTO))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isOk
            .expectBody<TaskDTO>()
            .isEqualTo(taskDTO.copy(id = 999))
    }

    @Test
    fun `POST task returns BAD_REQUEST if called with invalid body`() {
        // when
        webTestClient.post()
            .uri("$controllerRequestMapping/task")
            .body<TaskDTO>(CompletableDeferred(""))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isBadRequest
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task",
                    HttpStatus.BAD_REQUEST
                )
            }
    }

    @Test
    fun `POST task returns INTERNAL_SERVER_ERROR if save fails`() {
        // setup
        val taskDTO = createTaskDTO()
        coEvery { taskService.create(any()) } throws IllegalArgumentException("save failed due to a mocked error")
        // when
        webTestClient.post()
            .uri("$controllerRequestMapping/task")
            .body<TaskDTO>(CompletableDeferred(taskDTO))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task",
                    HttpStatus.INTERNAL_SERVER_ERROR
                )
            }
    }

    @Test
    fun `PUT task-id updates an existing task`() {
        // setup
        val taskDTO = createTaskDTO(description = "task update")
        val taskDTOWithId = taskDTO.copy(id=2)
        coEvery { taskService.update(any()) } answers {
            firstArg<TaskDTO>().copy()
        }
        // when
        webTestClient.put()
            .uri("$controllerRequestMapping/task/2")
            .body<TaskDTO>(CompletableDeferred(taskDTO))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isOk
            .expectBody<TaskDTO>()
            .isEqualTo(taskDTOWithId)
    }

    @Test
    fun `PUT task-id returns BAD_REQUEST if id not a positive integer`() {
        // when
        webTestClient.put()
            .uri("$controllerRequestMapping/task/ABC")
            .body<TaskDTO>(CompletableDeferred(createTaskDTO()))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isBadRequest
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/ABC",
                    HttpStatus.BAD_REQUEST
                )
            }
    }

    @Test
    fun `PUT task-id returns BAD_REQUEST if called with invalid body`() {
        // when
        webTestClient.put()
            .uri("$controllerRequestMapping/task/2")
            .body<TaskDTO>(CompletableDeferred(""))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isBadRequest
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/2",
                    HttpStatus.BAD_REQUEST
                )
            }
    }

    @Test
    fun `PUT task-id returns NOT_FOUND for nonexistent task`() {
        // setup
        coEvery { taskService.update(any()) } returns null
        // when
        webTestClient.put()
            .uri("$controllerRequestMapping/task/999")
            .body<TaskDTO>(CompletableDeferred(createTaskDTO()))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isNotFound
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/999",
                    HttpStatus.NOT_FOUND
                )
            }
    }

    @Test
    fun `DELETE task-id returns NO_CONTENT on success`() {
        // setup
        coEvery { taskService.delete(2) } returns true
        coEvery { userService.getCurrentUser() } returns "mock of current user"
        // when
        webTestClient.delete()
            .uri("$controllerRequestMapping/task/2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isNoContent
            .expectBody<Any>()
            .consumeWith { result ->
                assertThat(result.responseBody).isNull()
            }
    }

    @Test
    fun `DELETE task-id returns NOT_FOUND for nonexistent task`() {
        // setup
        coEvery { taskService.delete(2) } returns false
        coEvery { userService.getCurrentUser() } returns "mock of current user"
        // when
        webTestClient.delete()
            .uri("$controllerRequestMapping/task/2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isNotFound
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/2",
                    HttpStatus.NOT_FOUND
                )
            }
    }

    @Test
    fun `DELETE task-id returns BAD_REQUEST if id is not a positive integer`() {
        // setup
        coEvery { taskService.delete(any()) } returns true
        coEvery { userService.getCurrentUser() } returns "mock of current user"
        // when
        webTestClient.delete()
            .uri("$controllerRequestMapping/task/ABC")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            // then
            .expectStatus().isBadRequest
            .expectBody<Map<String, String>>()
            .consumeWith {
                assertErrorResponse(
                    it.responseBody,
                    "$controllerRequestMapping/task/ABC",
                    HttpStatus.BAD_REQUEST
                )
            }
    }

    private fun assertErrorResponse(apiError: Map<String, String>?, path: String, httpStatus: HttpStatus) {
        assertThat(apiError).isNotNull()
        val error = apiError!!
        assertThat(error["timestamp"]).isNotBlank()
        assertThat(error["path"]).isEqualTo(path)
        assertThat(error["status"]).isEqualTo(httpStatus.value().toString())
        assertThat(error["error"]).isEqualTo(httpStatus.reasonPhrase)
        assertThat(error["message"]).isNotNull()
        assertThat(error["requestId"]).isNotBlank()
    }

}
