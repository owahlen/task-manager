package org.taskmanager.task.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.taskmanager.task.exception.AuthenticationTokenNotFoundException

/**
 * Inspired by
 * https://github.com/rbiedrawa/spring-webflux-keycloak-demo/blob/master/src/main/java/com/rbiedrawa/oauth/web/AboutMeController.java
 */
@RestController
@RequestMapping("/me")
class AboutMeController() {

    @Operation(
        summary = "Get token attributes",
        responses = [
            ApiResponse(responseCode = "200", description = "got token attributes"),
            ApiResponse(responseCode = "401", description = "no authentication token was found")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    suspend fun getClaims(@AuthenticationPrincipal auth: JwtAuthenticationToken?): Map<String, Any> {
        if (auth == null) throw AuthenticationTokenNotFoundException()
        return auth.tokenAttributes
    }

    @Operation(
        summary = "Get principal name",
        responses = [
            ApiResponse(responseCode = "200", description = "got principal name"),
            ApiResponse(responseCode = "401", description = "no authentication token was found")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/name", produces = [TEXT_PLAIN_VALUE])
    suspend fun getName(@AuthenticationPrincipal auth: JwtAuthenticationToken?): String {
        if (auth == null) throw AuthenticationTokenNotFoundException()
        return auth.name
    }

    @Operation(
        summary = "Get token",
        responses = [
            ApiResponse(responseCode = "200", description = "got token value"),
            ApiResponse(responseCode = "401", description = "no authentication token was found")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/token", produces = [TEXT_PLAIN_VALUE])
    suspend fun getToken(@AuthenticationPrincipal auth: JwtAuthenticationToken?): String {
        if (auth == null) throw AuthenticationTokenNotFoundException()
        return auth.token.tokenValue
    }

    @Operation(
        summary = "Test if user has role Admin",
        responses = [
            ApiResponse(responseCode = "200", description = "returned if ROLE_ADMIN is granted"),
            ApiResponse(responseCode = "401", description = "ROLE_ADMIN is not granted")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/roleAdmin", produces = [TEXT_PLAIN_VALUE])
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    suspend fun roleAdmin(): String {
        return "You have ROLE_ADMIN"
    }

    @Operation(
        summary = "Test if user has SCOPE_MESSAGES:READ",
        responses = [
            ApiResponse(responseCode = "200", description = "returned if SCOPE_MESSAGES:READ is granted"),
            ApiResponse(responseCode = "403", description = "SCOPE_MESSAGES:READ is not granted")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/scopeMessagesRead", produces = [TEXT_PLAIN_VALUE])
    @PreAuthorize("hasRole('SCOPE_MESSAGES:READ')")
    suspend fun scopeMessagesRead(): String {
        return "'SCOPE_MESSAGES:READ' is granted"
    }
}
