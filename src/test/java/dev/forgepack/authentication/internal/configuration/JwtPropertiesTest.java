package dev.forgepack.authentication.internal.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void toString_withBlankSecret_masksAsRandom() {
        JwtProperties properties = new JwtProperties("issuer", "audience", 1000L, "");
        assertThat(properties.toString()).contains("<random>");
    }

    @Test
    void toString_withConfiguredSecret_masksValue() {
        JwtProperties properties = new JwtProperties("issuer", "audience", 1000L, "supersecret");
        assertThat(properties.toString()).contains("***").doesNotContain("supersecret");
    }

    @Test
    void accessors_returnConstructorValues() {
        JwtProperties properties = new JwtProperties("issuer", "audience", 1000L, "secret");
        assertThat(properties.issuer()).isEqualTo("issuer");
        assertThat(properties.audience()).isEqualTo("audience");
        assertThat(properties.expiration()).isEqualTo(1000L);
        assertThat(properties.secret()).isEqualTo("secret");
    }
}
