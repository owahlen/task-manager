package com.example.rest.configuration

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor
import org.springframework.web.reactive.function.server.ServerRequest

@Configuration
class ErrorConfiguration {

    @Bean
    fun apiErrorAttributes() = object : DefaultErrorAttributes() {
        override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): MutableMap<String, Any> {
            val errorAttributes = super.getErrorAttributes(request, options)
            if (errorAttributes.get("message") == null) {
                errorAttributes.put("message", "")
            }
            return errorAttributes
        }
    }

    @Bean
    fun validationPostProcessor() = MethodValidationPostProcessor()
}
