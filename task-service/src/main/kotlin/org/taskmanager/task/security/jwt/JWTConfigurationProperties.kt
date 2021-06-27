package org.taskmanager.task.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "security.jwt")
data class JWTConfigurationProperties(
    val secret: String? = null,
    val base64Secret: String? = null,
    val tokenValidityInSeconds: Long = 1800, // 30 minutes
    val tokenValidityInSecondsForRememberMe: Long = 2592000 // 30 days
)
