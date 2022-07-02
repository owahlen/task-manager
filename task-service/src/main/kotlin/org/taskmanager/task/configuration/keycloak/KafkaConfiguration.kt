package org.taskmanager.task.configuration.keycloak

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.taskmanager.task.kafka.KeycloakAdminEvent
import org.taskmanager.task.kafka.KeycloakEvent
import org.taskmanager.task.kafka.KeycloakKafkaConsumer


@EnableKafka
@Configuration
class KafkaConfiguration(
    @Value("\${kafka.bootstrapAddress}")
    private val bootstrapAddress: String,
    @Value("\${keycloak.kafka.group-id}")
    private val groupId: String,
    private val keycloakKafkaConsumer: KeycloakKafkaConsumer
) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props: Map<String, Any> = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun jsonConverter(): StringJsonMessageConverter {
        return StringJsonMessageConverter()
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(consumerFactory())
        factory.setMessageConverter(jsonConverter())
        return factory
    }

    @KafkaListener(topics = ["\${keycloak.kafka.events-topic}"])
    fun handleKeycloakEvent(event: KeycloakEvent) {
        keycloakKafkaConsumer.handleKeycloakEvent(event)
    }

    @KafkaListener(topics = ["\${keycloak.kafka.admin-events-topic}"])
    fun handleKeycloakAdminEvent(event: KeycloakAdminEvent) {
        keycloakKafkaConsumer.handleKeycloakAdminEvent(event)
    }

}
