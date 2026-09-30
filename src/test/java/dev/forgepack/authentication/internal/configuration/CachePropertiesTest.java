package dev.forgepack.authentication.internal.configuration;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CachePropertiesTest {

    @Test
    void defaultConstructor_appliesDefaults() {
        CacheProperties properties = new CacheProperties();
        assertThat(properties.ttl()).isEqualTo(Duration.ofHours(1));
        assertThat(properties.refresh()).isEqualTo(Duration.ofMinutes(45));
        assertThat(properties.initialCapacity()).isEqualTo(100);
        assertThat(properties.maximumSize()).isEqualTo(5000);
    }

    @Test
    void customValues_areAccepted() {
        CacheProperties properties = new CacheProperties(Duration.ofMinutes(30), Duration.ofMinutes(10), 5, 50);
        assertThat(properties.ttl()).isEqualTo(Duration.ofMinutes(30));
        assertThat(properties.refresh()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void refreshGreaterThanOrEqualToTtl_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new CacheProperties(Duration.ofMinutes(10), Duration.ofMinutes(10), 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new CacheProperties(Duration.ofMinutes(10), Duration.ofMinutes(15), 1, 1));
    }
}
