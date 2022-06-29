package keycloak.kafka;

import org.junit.jupiter.api.Test;
import org.keycloak.Config.SystemPropertiesConfigProvider;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaProducerConfigTests {

    @Test
    void shouldReturnMapWithConfigWhenPropertyExists() {
        System.setProperty("keycloak.retry.backoff.ms", "1000");
        System.setProperty("keycloak.max.block.ms", "5000");
        System.setProperty("keycloak.foo", "bar");

        Map<String, Object> config = KafkaProducerConfig.init(new SystemPropertiesConfigProvider().scope());
        Map<String, Object> expected = new HashMap<>();
        expected.put("retry.backoff.ms", "1000");
        expected.put("max.block.ms", "5000");
        assertEquals(expected, config);
    }
}
