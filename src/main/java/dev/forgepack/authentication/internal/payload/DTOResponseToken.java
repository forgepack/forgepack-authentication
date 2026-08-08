package dev.forgepack.authentication.internal.payload;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing authentication token responses.
 *
 * <p>This class encapsulates the result of authentication operations, such as
 * login or token refresh, providing both access and refresh tokens along with
 * the associated authorization context (roles).</p>
 *
 * <p>Extends {@link org.springframework.hateoas.RepresentationModel} to support
 * Hypermedia as the Engine of Application State (HATEOAS), enabling the inclusion
 * of navigational links in API responses.</p>
 *
 * <h3>Core Attributes</h3>
 * <ul>
 *     <li><b>tokenType</b>: token scheme used for authorization (typically "Bearer")</li>
 *     <li><b>accessToken</b>: short-lived token used to access protected resources</li>
 *     <li><b>refreshToken</b>: long-lived token used to obtain new access tokens</li>
 *     <li><b>role</b>: set of roles granted to the authenticated user</li>
 * </ul>
 *
 * <h3>Usage Context</h3>
 * <ul>
 *     <li>Returned by authentication endpoints (e.g., login, refresh)</li>
 *     <li>Used by clients to authorize subsequent API requests</li>
 *     <li>Compatible with stateless authentication strategies (e.g., JWT)</li>
 * </ul>
 *
 * <h3>HATEOAS Support</h3>
 * <ul>
 *     <li>Allows inclusion of hypermedia links via {@link Link}</li>
 *     <li>Facilitates discoverability of related authentication actions</li>
 * </ul>
 *
 * <h3>Security Considerations</h3>
 * <ul>
 *     <li>Tokens must be transmitted exclusively over secure channels (HTTPS)</li>
 *     <li>Access tokens should be short-lived to reduce exposure risk</li>
 *     <li>Refresh tokens must be securely stored and protected</li>
 *     <li>Sensitive data (tokens) should not be logged or exposed in plain text</li>
 * </ul>
 *
 * <h3>Architectural Notes</h3>
 * <ul>
 *     <li>This DTO is output-only and does not contain persistence logic</li>
 *     <li>Should be constructed by the authentication service or an assembler layer</li>
 *     <li>Encapsulates both authentication result and authorization context</li>
 * </ul>
 *
 * @author Marcelo Ribeiro Gadelha
 * @version 1.0
 * @since 1.0
 *
 * @see org.springframework.hateoas.RepresentationModel
 */
public class DTOResponseToken extends RepresentationModel<DTOResponseToken> {

    private final String tokenType = "Bearer ";
    private String accessToken;
    private final UUID refreshToken;
    private Set<String> role;

    public DTOResponseToken(UUID refreshToken) {
        this.refreshToken = refreshToken;
    }
    public DTOResponseToken(String accessToken, UUID refreshToken, Set<String> role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public String getTokenType() {
        return tokenType;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public UUID getRefreshToken() {
        return refreshToken;
    }
    public Set<String> getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "DTOResponseToken{" +
                "tokenType='" + tokenType + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken=" + refreshToken +
                ", role=" + role +
                '}';
    }
}
