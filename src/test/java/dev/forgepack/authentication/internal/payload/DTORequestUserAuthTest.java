package dev.forgepack.authentication.internal.payload;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DTORequestUserAuthTest {

    @Test
    void constructor_setsAllFields() {
        UUID id = UUID.randomUUID();
        DTORequestUserAuth dto = new DTORequestUserAuth(id, "john", "Password1!", 123456);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.username()).isEqualTo("john");
        assertThat(dto.password()).isEqualTo("Password1!");
        assertThat(dto.secret()).isEqualTo(123456);
    }

    @Test
    void equalsAndHashCode_areRecordBased() {
        UUID id = UUID.randomUUID();
        DTORequestUserAuth first = new DTORequestUserAuth(id, "john", "Password1!", 123456);
        DTORequestUserAuth second = new DTORequestUserAuth(id, "john", "Password1!", 123456);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
