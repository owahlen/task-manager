package com.example.rest.task

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpMethod
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TaskRouterTest {

    private val taskHandler = mockk<TaskHandler>()
    private val taskRouter = TaskRouter()
    private lateinit var client : WebTestClient

    @BeforeAll
    fun beforeAll() {
        val routerFunction = taskRouter.taskRoute(taskHandler)
        client = WebTestClient.bindToRouterFunction(routerFunction).build()
    }

    @Test
    fun `apis are routed correctly`() {
        validateRoute(HttpMethod.GET, "/api/task", taskHandler::findAll)
        validateRoute(HttpMethod.GET,"/api/task/search", taskHandler::search)
        validateRoute(HttpMethod.GET,"/api/task/1", taskHandler::findTask)
        validateRoute(HttpMethod.POST,"/api/task", taskHandler::createTask)
        validateRoute(HttpMethod.PUT,"/api/task/1", taskHandler::updateTask)
        validateRoute(HttpMethod.DELETE,"/api/task/1", taskHandler::deleteTask)
    }

    @Suppress("ReactiveStreamsUnusedPublisher")
    private fun validateRoute(httpMethod: HttpMethod, uri: String, f: suspend (ServerRequest) -> ServerResponse) {
        // setup handler to return the mock response
        val response = mockk<ServerResponse>()
        coEvery { f(any()) } returns response
        every { response.writeTo(any(),any()) } returns Mono.empty()
        // call the defined method and uri
        client.method(httpMethod).uri(uri).exchange()
        // check that the handler f has been called with the right parameters
        val captureData = slot<ServerRequest>()
        coVerify { f(capture(captureData)) }
        assertThat(captureData.captured.method()).isEqualTo(httpMethod)
        assertThat(captureData.captured.path()).isEqualTo(uri)
    }
}