package dev.forgepack.authentication.internal.payload;

import dev.forgepack.core.api.payload.DTOIdentifiable;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for token-related requests.
 *
 * <p>This record represents the data required for operations involving
 * authentication tokens, such as refreshing access tokens or handling
 * token-based authentication flows.</p>
 *
 * <h3>Core Attributes</h3>
 * <ul>
 *     <li><b>tokenType</b>: type of the token (e.g., "Bearer")</li>
 *     <li><b>accessToken</b>: short-lived token used to access protected resources</li>
 *     <li><b>refreshToken</b>: long-lived token used to obtain new access tokens</li>
 * </ul>
 *
 * <h3>Validation Rules</h3>
 * <ul>
 *     <li><b>refreshToken</b>: required and must be a valid UUID</li>
 * </ul>
 *
 * <h3>Usage Context</h3>
 * <ul>
 *     <li>Used in authentication flows involving token refresh or validation</li>
 *     <li>Supports stateless security architectures (e.g., JWT-based authentication)</li>
 *     <li>Commonly exchanged between client and server in secure communication channels</li>
 * </ul>
 *
 * <h3>Security Considerations</h3>
 * <ul>
 *     <li>Tokens must be transmitted over secure channels (HTTPS)</li>
 *     <li>Access tokens should have short expiration times</li>
 *     <li>Refresh tokens must be stored securely and handled with care</li>
 * </ul>
 *
 * <h3>Architectural Notes</h3>
 * <ul>
 *     <li>This DTO does not contain persistence logic</li>
 *     <li>Should be processed by the authentication service layer</li>
 *     <li>May be used in conjunction with token rotation and revocation strategies</li>
 * </ul>
 *
 * @param id optional identifier (not typically required for token operations)
 * @param tokenType token type (default: "Bearer ")
 * @param accessToken access token used for resource access
 * @param refreshToken refresh token used to obtain new access tokens
 *
 * @author Marcelo Ribeiro Gadelha
 * @version 1.0
 * @since 1.0
 *
 * @see DTOIdentifiable
 */
public record TokenRequest(

        UUID id,
        String tokenType,
        String accessToken,
        @NotNull
        UUID refreshToken
) implements DTOIdentifiable<UUID> {

    public TokenRequest(UUID id, String accessToken, UUID refreshToken) {
        this(id, "Bearer ", accessToken, refreshToken);
    }
}
