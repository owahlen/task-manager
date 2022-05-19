package org.taskmanager.task.api.controller


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import org.taskmanager.task.api.resource.PersonCreateResource
import org.taskmanager.task.api.resource.PersonPatchResource
import org.taskmanager.task.api.resource.PersonResource
import org.taskmanager.task.api.resource.PersonUpdateResource
import org.taskmanager.task.mapper.toPerson
import org.taskmanager.task.mapper.toPersonResource
import org.taskmanager.task.model.Person
import org.taskmanager.task.service.PersonService
import javax.validation.Valid

@RestController
@RequestMapping("/person")
class PersonController(private val personService: PersonService) {

    @Operation(
        summary = "Get page of persons",
        responses = [ApiResponse(responseCode = "200", description = "got page of persons")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun getAllPersons(
        @PageableDefault(value = 100, sort = ["firstName", "lastName"], direction = Sort.Direction.ASC)
        pageable: Pageable
    ): Page<PersonResource> {
        return personService.findAllBy(pageable).map(Person::toPersonResource)
    }

    @Operation(
        summary = "Get a specific person",
        responses = [ApiResponse(responseCode = "200", description = "got person by id"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "person not found")]
    )
    @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun getPersonById(@PathVariable id: Long): PersonResource {
        return personService.getById(id).toPersonResource()
    }

    @Operation(
        summary = "Create a person",
        responses = [ApiResponse(responseCode = "200", description = "person created"),
            ApiResponse(responseCode = "400", description = "bad parameter")]
    )
    @PostMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun createPerson(@Valid @RequestBody personCreateResource: PersonCreateResource): PersonResource {
        return personService.create(personCreateResource.toPerson()).toPersonResource()
    }

    @Operation(
        summary = "Update a person",
        responses = [ApiResponse(responseCode = "200", description = "person updated"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "person not found")]
    )
    @PutMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun updatePerson(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody personUpdateResource: PersonUpdateResource
    ): PersonResource {
        return personService.update(personUpdateResource.toPerson(id, version)).toPersonResource()
    }

    @Operation(
        summary = "Patch a person",
        responses = [ApiResponse(responseCode = "200", description = "person patched"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "person not found")]
    )
    @PatchMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
    suspend fun patchPerson(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?,
        @Valid @RequestBody personPatchResource: PersonPatchResource
    ): PersonResource {
        val person = personService.getById(id, version)
        return personService.update(personPatchResource.toPerson(person)).toPersonResource()
    }

    @Operation(
        summary = "Delete a person",
        responses = [ApiResponse(responseCode = "200", description = "person deleted"),
            ApiResponse(responseCode = "400", description = "bad parameter"),
            ApiResponse(responseCode = "404", description = "person not found")]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deletePerson(
        @PathVariable id: Long,
        @RequestHeader(value = HttpHeaders.IF_MATCH) version: Long?
    ) {
        personService.deleteById(id, version)
    }
}
