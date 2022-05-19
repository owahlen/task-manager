package org.taskmanager.task.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver

@Configuration
class WebConfig {

    @Bean
    fun reactivePageableHandlerMethodArgumentResolver(): HandlerMethodArgumentResolver {
        //  allows injecting Pageable instances into WebFlux controller methods
        return ReactivePageableHandlerMethodArgumentResolver()
    }
}