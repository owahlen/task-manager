package org.taskmanager.task.configuration

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor
import org.springframework.web.reactive.function.server.ServerRequest
import java.util.*
import javax.validation.ClockProvider
import javax.validation.MessageInterpolator
import javax.validation.Validator
import javax.validation.ValidatorFactory

@Configuration
class ErrorConfiguration {

    /**
     * Use the default strategy for sending JSON errors
     * but never send the string "null" as message and hide messages of Internal Server Errors (HttpStatus 500)
     */
    @Bean
    fun apiErrorAttributes() = object : DefaultErrorAttributes() {
        override fun getErrorAttributes(
            request: ServerRequest,
            options: ErrorAttributeOptions
        ): MutableMap<String, Any> {
            val errorAttributes = super.getErrorAttributes(request, options)
            if (errorAttributes["message"] == null || errorAttributes["status"] == 500) {
                // hide messages from Internal Server Errors
                errorAttributes["message"] = ""
            }
            return errorAttributes
        }
    }

    /**
     * Use the default validation strategy of the Spring framework but always return english error messages
     */
    @Bean
    fun validator(): Validator {
        return object : LocalValidatorFactoryBean() {
            override fun getClockProvider(): ClockProvider = unwrap(ValidatorFactory::class.java).clockProvider
            override fun postProcessConfiguration(configuration: javax.validation.Configuration<*>) {
                val targetInterpolator = configuration.getDefaultMessageInterpolator()
                configuration.messageInterpolator(object : LocaleContextMessageInterpolator(targetInterpolator) {
                    override fun interpolate(message: String, context: MessageInterpolator.Context): String {
                        return super.interpolate(message, context, Locale.US)
                    }

                    override fun interpolate(
                        message: String,
                        context: MessageInterpolator.Context,
                        locale: Locale
                    ): String {
                        return super.interpolate(message, context, Locale.US)
                    }
                })
            }
        }
    }

    @Bean
    fun validationPostProcessor() = MethodValidationPostProcessor()

}
