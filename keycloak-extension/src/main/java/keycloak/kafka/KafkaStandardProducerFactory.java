package keycloak.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;
import java.util.Properties;

public final class KafkaStandardProducerFactory implements KafkaProducerFactory {

    @Override
    public Producer<String, String> createProducer(String clientId, String bootstrapServer,
                                                   Map<String, Object> optionalProperties) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.putAll(optionalProperties);

        // fix Class org.apache.kafka.common.serialization.StringSerializer could not be
        // found. see https://stackoverflow.com/a/50981469
        Thread.currentThread().setContextClassLoader(null);

        return new KafkaProducer<>(props);
    }
}
