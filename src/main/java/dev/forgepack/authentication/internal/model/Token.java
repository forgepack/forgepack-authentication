package dev.forgepack.authentication.internal.model;

import dev.forgepack.core.api.model.EntityCrud;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Domain entity representing a refresh token used in authentication flows.
 *
 * <p>This entity is responsible for persisting refresh tokens issued to clients,
 * typically used in token-based authentication mechanisms (e.g., JWT) to obtain
 * new access tokens without requiring re-authentication.</p>
 *
 * <h3>Core Attributes</h3>
 * <ul>
 *     <li><b>refreshToken</b>: unique identifier of the refresh token (UUID-based)</li>
 *     <li><b>active</b>: indicates whether the token is valid or has been revoked</li>
 * </ul>
 *
 * <h3>Security Considerations</h3>
 * <ul>
 *     <li>Refresh tokens should be treated as sensitive credentials and stored securely</li>
 *     <li>The <b>active</b> flag enables token revocation strategies (e.g., logout, compromise mitigation)</li>
 *     <li>Token rotation strategies can be implemented on top of this entity</li>
 * </ul>
 *
 * <h3>Usage Context</h3>
 * <ul>
 *     <li>Commonly used in conjunction with access tokens in stateless authentication architectures</li>
 *     <li>Can be associated with a user session or device (not modeled directly in this entity)</li>
 * </ul>
 *
 * <h3>Auditing</h3>
 * <p>Auditing is enabled via {@link org.hibernate.envers.Audited},
 * inheriting lifecycle metadata from {@link EntityCrud}.</p>
 *
 * <h3>Architectural Notes</h3>
 * <ul>
 *     <li>This entity focuses on persistence and lifecycle management of refresh tokens</li>
 *     <li>Validation and business rules (e.g., expiration, ownership) should be handled at the service layer</li>
 * </ul>
 *
 * @author Marcelo Ribeiro Gadelha
 * @version 1.0
 * @since 1.0
 *
 * @see EntityCrud
 */
@Entity
@Table
public class Token extends EntityCrud {

    private UUID refreshToken;
    private boolean active;

    public Token() {
    }
    public Token(UUID refreshToken) {
        this.refreshToken = refreshToken;
    }
    public Token(UUID refreshToken, boolean active) {
        this.refreshToken = refreshToken;
        this.active = active;
    }

    public void setRefreshToken(UUID refreshToken) {
        this.refreshToken = refreshToken;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getRefreshToken() {
        return refreshToken;
    }
    public boolean isActive() {
        return active;
    }
}
