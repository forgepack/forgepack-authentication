package dev.forgepack.authentication.internal.mapper;

import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class MapperToken implements Mapper<Token, DTORequestToken, DTOResponseToken> {

    private MapperToken() {}

    @Override
    public Token toEntity(DTORequestToken dto) {
        if (dto == null) return null;
        return new Token(
                dto.refreshToken()
        );
    }

    @Override
    public DTOResponseToken toResponse(Token entity) {
        if (entity == null) return null;
        return new DTOResponseToken(
                entity.getId()
        );
    }

    @Override
    public void updateEntity(DTORequestToken dto, Token entity) {
        if (dto == null || entity == null) return;
        entity.setRefreshToken(dto.refreshToken());
    }

    @Override
    public Set<DTOResponseToken> toResponseSet(Set<Token> entities) {
        if (entities == null) return Set.of();
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
}
