package dev.forgepack.authentication.internal.mapper;

import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import dev.forgepack.core.api.model.EntityCrud;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTokenTest {

    private final MapperToken mapper = newMapperToken();

    private static MapperToken newMapperToken() {
        try {
            var constructor = MapperToken.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setId(Token token, UUID id) throws Exception {
        Field field = EntityCrud.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(token, id);
    }

    @Test
    void toEntity_mapsRefreshToken() {
        UUID refreshToken = UUID.randomUUID();
        DTORequestToken dto = new DTORequestToken(null, "access-token", refreshToken);

        Token entity = mapper.toEntity(dto);

        assertThat(entity.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void toEntity_null_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toResponse_mapsEntityId() throws Exception {
        Token token = new Token(UUID.randomUUID());
        UUID id = UUID.randomUUID();
        setId(token, id);

        DTOResponseToken response = mapper.toResponse(token);

        assertThat(response.getRefreshToken()).isEqualTo(id);
    }

    @Test
    void toResponse_null_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void updateEntity_updatesRefreshToken() {
        Token entity = new Token(UUID.randomUUID());
        UUID newRefreshToken = UUID.randomUUID();
        DTORequestToken dto = new DTORequestToken(null, "access-token", newRefreshToken);

        mapper.updateEntity(dto, entity);

        assertThat(entity.getRefreshToken()).isEqualTo(newRefreshToken);
    }

    @Test
    void updateEntity_nullArguments_doesNotThrow() {
        mapper.updateEntity(null, null);
        Token entity = new Token(UUID.randomUUID());
        mapper.updateEntity(null, entity);
    }

    @Test
    void toResponseSet_null_returnsEmptySet() {
        assertThat(mapper.toResponseSet(null)).isEmpty();
    }

    @Test
    void toResponseSet_mapsAllEntities() {
        Token token = new Token(UUID.randomUUID());
        Set<DTOResponseToken> result = mapper.toResponseSet(Set.of(token));
        assertThat(result).hasSize(1);
    }
}
