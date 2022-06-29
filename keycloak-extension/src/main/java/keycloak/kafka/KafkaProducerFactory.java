package keycloak.kafka;

import org.apache.kafka.clients.producer.Producer;

import java.util.Map;

public interface KafkaProducerFactory {

    Producer<String, String> createProducer(String clientId, String bootstrapServer,
                                            Map<String, Object> optionalProperties);

}
