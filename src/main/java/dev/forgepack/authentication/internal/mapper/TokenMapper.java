package dev.forgepack.authentication.internal.mapper;

import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public final class TokenMapper implements Mapper<Token, TokenRequest, TokenResponse> {

    private TokenMapper() {}

    @Override
    public Token toEntity(TokenRequest dto) {
        if (dto == null) return null;
        return new Token(
                dto.refreshToken()
        );
    }

    @Override
    public TokenResponse toResponse(Token entity) {
        if (entity == null) return null;
        return new TokenResponse(
                entity.getId()
        );
    }

    @Override
    public void updateEntity(TokenRequest dto, Token entity) {
        if (dto == null || entity == null) return;
        entity.setRefreshToken(dto.refreshToken());
    }

    @Override
    public Set<TokenResponse> toResponseSet(Set<Token> entities) {
        if (entities == null) return Set.of();
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
}
