package org.taskmanager.task.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.taskmanager.task.configuration.DatabaseConfiguration
import org.taskmanager.task.domain.Task
import org.taskmanager.task.repository.TaskRepository
import org.taskmanager.task.service.dto.TaskDTO
import org.taskmanager.task.service.mapper.toTask

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DataR2dbcTest
@Import(DatabaseConfiguration::class)
internal class TaskServiceIT {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    private lateinit var taskService: TaskService

    private val testTaskIds = ArrayList<Long>()

    @BeforeAll
    fun beforeAll() {
        taskService = TaskService(taskRepository)
        runBlocking {
            initDatabase().map { it.id!! }.toList(testTaskIds)
        }
    }

    @AfterEach
    fun afterEach() {
        runBlocking {
            val cleanupTasks = taskRepository.findAll().filter { !testTaskIds.contains(it.id) }
            taskRepository.deleteAll(cleanupTasks)
        }
    }

    @Test
    @Order(1)
    fun `findAll returns values`() {
        runBlocking {
            // when
            val resp = taskService.findAll()
            // then
            assertThat(resp.count()).isEqualTo(3)
        }
    }

    @Test
    fun `findById returns a value`() {
        runBlocking {
            // setup
            val task1Id = testTaskIds.first()
            // when
            val resp = taskService.findById(task1Id)
            // then
            assertThat(resp).isNotNull()
            assertThat(resp?.description).isEqualTo("Task1")
        }
    }

    @Test
    fun `findById returns a null if value does not exists`() {
        runBlocking {
            // when
            val resp = taskService.findById(999)
            // then
            assertThat(resp).isNull()
        }
    }

    @Test
    fun `findByCompleted returns a value`() {
        runBlocking {
            // setup
            val taskDTO = TaskDTO(null, "Completed task", true)
            val completedTask = taskRepository.save(taskDTO.toTask())
            assertThat(completedTask).isNotNull()
            // when
            val resp = taskService.findByCompleted(true)
            // then
            assertThat(resp.count()).isEqualTo(1)
            resp.map {
                assertThat(it.description).isEqualTo("Task2")
            }
        }
    }

    @Test
    fun `findByCompleted returns an empty flow if no task is completed`() {
        runBlocking {
            // when
            val resp = taskService.findByCompleted(true)
            // then
            assertThat(resp.count()).isEqualTo(0)
        }
    }

    @Test
    fun `create creates a task`() {
        runBlocking {
            // setup
            val taskDTO = TaskDTO(null, "New task", true)
            // when
            val resp = taskService.create(taskDTO)
            // then
            assertThat(resp.id).isNotNull()
            val dbTask = taskRepository.findById(resp.id!!)
            assertThat(dbTask).isNotNull()
            assertThat(dbTask!!.description).isEqualTo("New task")
        }
    }

    @Test
    fun `update updates a task`() {
        runBlocking {
            // setup
            val taskDTO = TaskDTO(null,"Task to update", true)
            val newTask = createTask(taskDTO)
            val newTaskId = newTask.id!!
            val updateTaskDTO = taskDTO.copy(id=newTaskId, description = "Updated task")
            // when
            val resp = taskService.update(updateTaskDTO)
            // then
            assertThat(resp).isNotNull()
            assertThat(resp?.id).isNotNull()
            val dbTask = taskRepository.findById(newTaskId)
            assertThat(dbTask).isNotNull()
            assertThat(dbTask!!.description).isEqualTo("Updated task")
        }
    }

    @Test
    fun `update nonexistent task returns null`() {
        runBlocking {
            // setup
            val nonexistentTaskDTO = TaskDTO(999,"Nonexistent", false)
            // when
            val resp = taskService.update(nonexistentTaskDTO)
            // then
            assertThat(resp).isNull()
        }
    }

    @Test
    fun `delete deletes existing task returns true`() {
        runBlocking {
            // setup
            val newTask = createTask(TaskDTO(null, "Task to delete", true))
            val newTaskId = newTask.id!!
            // when
            val resp = taskService.delete(newTaskId)
            // then
            assertThat(resp).isTrue()
            val dbTask = taskRepository.findById(newTaskId)
            assertThat(dbTask).isNull()
        }
    }

    @Test
    fun `delete deletes nonexistent task returns false`() {
        runBlocking {
            // when
            val resp = taskService.delete(9999)
            // then
            assertThat(resp).isFalse()
        }
    }

    private suspend fun initDatabase(): Flow<Task> {
        // delete all tasks
        taskRepository.deleteAll()
        // return Flow of new task creations
        val initData = flowOf(
            Task(null, "Task1", false),
            Task(null, "Task2", false),
            Task(null, "Task3", false)
        )
        return taskRepository.saveAll(initData)
    }

    private suspend fun createTask(taskDTO: TaskDTO): Task {
        val newTask = taskRepository.save(taskDTO.toTask())
        assertThat(newTask).isNotNull()
        assertThat(newTask.id).isNotNull()
        return newTask
    }
}
