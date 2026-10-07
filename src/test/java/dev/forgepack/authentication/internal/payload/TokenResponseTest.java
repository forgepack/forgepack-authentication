package dev.forgepack.authentication.internal.payload;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenResponseTest {

    @Test
    void singleArgConstructor_setsRefreshTokenOnly() {
        UUID refreshToken = UUID.randomUUID();
        TokenResponse response = new TokenResponse(refreshToken);

        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRole()).isNull();
        assertThat(response.getTokenType()).isEqualTo("Bearer ");
    }

    @Test
    void fullConstructor_setsAllFields() {
        UUID refreshToken = UUID.randomUUID();
        Set<String> roles = Set.of("ROLE_ADMIN");
        TokenResponse response = new TokenResponse("access-token", refreshToken, roles);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getRole()).isEqualTo(roles);
    }

    @Test
    void toString_containsFieldValues() {
        UUID refreshToken = UUID.randomUUID();
        TokenResponse response = new TokenResponse("access-token", refreshToken, Set.of("ROLE_ADMIN"));

        String result = response.toString();

        assertThat(result).contains("DTOResponseToken{");
        assertThat(result).contains("access-token");
        assertThat(result).contains(refreshToken.toString());
    }
}
