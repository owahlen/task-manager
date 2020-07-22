package org.taskmanager.task.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service


@Service
class UserService {

    // inspired by https://www.naturalprogrammer.com/blog/16393/spring-security-get-current-user-programmatically
    suspend fun getCurrentUser(): Any? {
        return ReactiveSecurityContextHolder.getContext()
                .map { securityContext ->
                    securityContext.authentication.principal
                }
                .awaitFirstOrNull()
    }

}
