package com.example.rest.task

import com.example.rest.createTask
import com.example.rest.model.Task
import com.example.rest.model.TaskDTO
import com.example.rest.model.toModel
import com.example.rest.toDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

internal class TaskHandlerTest {

    private val taskService = mockk<TaskService>()
    private val taskHandler = TaskHandler(taskService)
    private val request = mockk<ServerRequest>()

    @Test
    fun `test exist findAll returns OK`() {
        coEvery { taskService.findAll() } returns flowOf(createTask(id=1), createTask(id=2))

        runBlocking {
            val response = taskHandler.findAll(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `test exist search returns OK`() {
        coEvery { taskService.findByCompleted(true) } returns flowOf(createTask(id=1), createTask(id=2, completed = true))
        every { request.queryParams() } returns LinkedMultiValueMap(mapOf("completed" to listOf("true")))

        runBlocking {
            val response = taskHandler.search(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `task does not exist findAll retuns OK`() {
        coEvery { taskService.findAll() } returns emptyFlow<Task>()

        runBlocking {
            val response = taskHandler.findAll(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)

        }
    }

    @Test
    fun `existing task findTask returns OK`() {

        coEvery { taskService.findById(1) } returns createTask(id = 1)
        every { request.pathVariable("id") } returns "1"

        runBlocking {
            val response = taskHandler.findTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `nonexistent task findTask returns NotFound`() {

        coEvery { taskService.findById(999) } returns null
        every { request.pathVariable("id") } returns "999"

        runBlocking {
            val response = taskHandler.findTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `path variable not a number findTask returns BadRequest`() {

        every { request.pathVariable("id") } returns "ABC"

        runBlocking {
            val response = taskHandler.findTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `creating a task createTask returns OK`() {
        coEvery { taskService.create(any()) } answers {
            firstArg<TaskDTO>().toModel().copy(id = 999)
        }
        every { request.bodyToMono<TaskDTO>() } returns createTask().toDto().toMono()

        runBlocking {
            val response = taskHandler.createTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
        }
    }

    @Test
    fun `invalid body createTask returns BadRequest`() {
        every { request.bodyToMono<TaskDTO>() } returns Mono.empty()

        runBlocking {
            val response = taskHandler.createTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `error on save createTask returns InternalServerError`() {
        coEvery { taskService.create(any()) } returns null
        every { request.bodyToMono<TaskDTO>() } returns createTask().toDto().toMono()

        runBlocking {
            val response = taskHandler.createTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Test
    fun `existing task updateTask returns OK`() {
        every { request.pathVariable("id") } returns "2"
        coEvery { taskService.update(2, any()) } answers {
            secondArg<TaskDTO>().toModel(firstArg<Long>())
        }
        every { request.bodyToMono<TaskDTO>() } returns createTask().toDto().toMono()

        runBlocking {
            val response = taskHandler.updateTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `id not a number updateTask returns BadRequest`() {
        every { request.pathVariable("id") } returns "BAD"

        runBlocking {
            val response = taskHandler.updateTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `bad body updateTask returns BadRequest`() {
        every { request.pathVariable("id") } returns "2"
        every { request.bodyToMono<TaskDTO>() } returns Mono.empty()

        runBlocking {
            val response = taskHandler.updateTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `nonexistent task updateTask returns NotFound`() {
        every { request.pathVariable("id") } returns "2"
        every { request.bodyToMono<TaskDTO>() } returns createTask().toDto().toMono()
        coEvery { taskService.update(2, any()) } returns null

        runBlocking {
            val response = taskHandler.updateTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `success deleteTask returns NoContent`() {
        every { request.pathVariable("id") } returns "2"
        coEvery { taskService.delete(2) } returns true

        runBlocking {
            val response = taskHandler.deleteTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT)
        }
    }

    @Test
    fun `nonexistent id deleteTask returns NotFound`() {
        every { request.pathVariable("id") } returns "2"
        coEvery { taskService.delete(2) } returns false

        runBlocking {
            val response = taskHandler.deleteTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `id not a number deleteTask returns BadRequest`() {
        every { request.pathVariable("id") } returns "BAD"

        runBlocking {
            val response = taskHandler.deleteTask(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

}