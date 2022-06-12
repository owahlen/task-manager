package org.taskmanager.task.configuration

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI documentation available under URL:
 * http://localhost:8080/api/v1/webjars/swagger-ui/index.html
 */
@Configuration
@OpenAPIDefinition(info = Info(title = "task-service API", version = "v1"))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class OpenApi30Configuration {
}
