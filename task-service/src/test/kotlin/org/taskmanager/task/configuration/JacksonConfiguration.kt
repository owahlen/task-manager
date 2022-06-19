package org.taskmanager.task.configuration

import com.fasterxml.jackson.databind.Module
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.taskmanager.task.testutils.PageJacksonModule
import org.taskmanager.task.testutils.SortJacksonModule


@Configuration
class JacksonConfiguration {

    @Bean
    fun pageJacksonModule(): Module {
        // allows deserialization of Page objects
        return PageJacksonModule()
    }

    @Bean
    fun sortJacksonModule(): Module {
        // allows deserialization of Sort objects
        return SortJacksonModule()
    }
}
