package org.taskmanager.task.configuration

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator

/**
 * Spring Data R2DBC currently does not accept the DB initialization using a schema.sql file.
 * Instead an initializer Bean must be used. Note that the connection is configured in the application.properties.
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto-initialize-a-database-using-r2dbc">Java Spec</a>
 */
@Configuration
class DatabaseConfiguration {

    @Bean
    fun dbInitializer(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val resourceLoader: ResourceLoader = DefaultResourceLoader()
        val populator = CompositeDatabasePopulator()
        arrayOf("schema.sql", "data.sql").forEach {
            val script = resourceLoader.getResource("classpath:$it")
            if (script.exists()) {
                populator.addPopulators(ResourceDatabasePopulator(script))
            }
        }
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        initializer.setDatabasePopulator(populator)
        return initializer
    }

}
