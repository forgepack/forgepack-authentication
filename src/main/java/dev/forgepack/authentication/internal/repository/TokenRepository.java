package dev.forgepack.authentication.internal.repository;

import dev.forgepack.core.api.repository.CrudRepository;
import dev.forgepack.authentication.internal.model.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends CrudRepository<Token> {

    Page<Token> findById(Pageable pageable, UUID uuid);
    Optional<Token> findByRefreshToken(UUID uuid);
    boolean existsByRefreshToken(UUID refreshToken);
}
