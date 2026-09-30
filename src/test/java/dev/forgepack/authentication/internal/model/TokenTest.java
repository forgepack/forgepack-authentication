package dev.forgepack.authentication.internal.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenTest {

    @Test
    void noArgConstructor_createsInactiveTokenWithNullRefreshToken() {
        Token token = new Token();
        assertThat(token.getRefreshToken()).isNull();
        assertThat(token.isActive()).isFalse();
    }

    @Test
    void refreshTokenOnlyConstructor_setsRefreshToken() {
        UUID refreshToken = UUID.randomUUID();
        Token token = new Token(refreshToken);
        assertThat(token.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(token.isActive()).isFalse();
    }

    @Test
    void fullConstructor_setsRefreshTokenAndActive() {
        UUID refreshToken = UUID.randomUUID();
        Token token = new Token(refreshToken, true);
        assertThat(token.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(token.isActive()).isTrue();
    }

    @Test
    void setters_updateState() {
        Token token = new Token();
        UUID refreshToken = UUID.randomUUID();
        token.setRefreshToken(refreshToken);
        token.setActive(true);
        assertThat(token.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(token.isActive()).isTrue();
    }
}
