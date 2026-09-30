package dev.forgepack.authentication.internal.configuration;

import io.jsonwebtoken.security.UnsupportedKeyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtConfigurationTest {

    private JwtConfiguration newConfiguration(long expiration, String secret) {
        JwtProperties properties = new JwtProperties("forgepack", "forgepack-client", expiration, secret);
        return new JwtConfiguration(properties);
    }

    @Test
    void generateAndValidateToken_withRandomKey_succeeds() {
        JwtConfiguration configuration = newConfiguration(60_000, "");
        String token = configuration.generateToken("john");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(configuration.validateJwt(token)).isTrue();
        assertThat(configuration.getUsernameFromJwt(token)).isEqualTo("john");
    }

    @Test
    void generateToken_withShortSecret_logsWarningButFailsToSign() {
        // HS512 requires a >= 64-byte key; a shorter configured secret only logs a warning
        // in buildSigningKey() but still cannot produce a valid signature.
        JwtConfiguration configuration = newConfiguration(60_000, "short-secret");
        assertThrows(UnsupportedKeyException.class, () -> configuration.generateToken("john"));
    }

    @Test
    void validateJwt_expiredToken_returnsFalse() throws InterruptedException {
        JwtConfiguration configuration = newConfiguration(1, "a".repeat(64));
        String token = configuration.generateToken("john");
        Thread.sleep(50);

        assertThat(configuration.validateJwt(token)).isFalse();
    }

    @Test
    void validateJwt_malformedToken_returnsFalse() {
        JwtConfiguration configuration = newConfiguration(60_000, "a".repeat(64));
        assertThat(configuration.validateJwt("not-a-valid-jwt")).isFalse();
    }

    @Test
    void validateJwt_wrongSigningKey_returnsFalse() {
        JwtConfiguration issuerConfig = newConfiguration(60_000, "a".repeat(64));
        JwtConfiguration verifierConfig = newConfiguration(60_000, "b".repeat(64));
        String token = issuerConfig.generateToken("john");

        assertThat(verifierConfig.validateJwt(token)).isFalse();
    }
}
