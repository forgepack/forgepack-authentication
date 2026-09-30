package dev.forgepack.authentication.internal.payload;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DTORequestTokenTest {

    @Test
    void fullConstructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        UUID refreshToken = UUID.randomUUID();
        DTORequestToken dto = new DTORequestToken(id, "Custom ", "access-token", refreshToken);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.tokenType()).isEqualTo("Custom ");
        assertThat(dto.accessToken()).isEqualTo("access-token");
        assertThat(dto.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void convenienceConstructor_defaultsToBearerTokenType() {
        UUID id = UUID.randomUUID();
        UUID refreshToken = UUID.randomUUID();
        DTORequestToken dto = new DTORequestToken(id, "access-token", refreshToken);

        assertThat(dto.tokenType()).isEqualTo("Bearer ");
        assertThat(dto.accessToken()).isEqualTo("access-token");
        assertThat(dto.refreshToken()).isEqualTo(refreshToken);
    }
}
