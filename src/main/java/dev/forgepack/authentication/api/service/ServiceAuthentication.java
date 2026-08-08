package dev.forgepack.authentication.api.service;

import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTORequestUserAuth;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import java.util.UUID;

/**
 * Service contract responsible for managing the user authentication lifecycle.
 *
 * <p>Defines the core operations for token-based authentication, including:</p>
 * <ul>
 *     <li>User authentication (login)</li>
 *     <li>Token renewal (refresh)</li>
 *     <li>Session termination (logout)</li>
 * </ul>
 *
 * <p>Implementations of this interface must ensure security, token integrity,
 * and compliance with the application's authentication policies
 * (e.g., JWT, MFA, expiration, and revocation strategies).</p>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 */
public interface ServiceAuthentication {

    /**
     * Authenticates a user based on the provided credentials.
     *
     * @param dtoRequestUserAuth object containing user authentication credentials
     *                           (e.g., username and password, optionally including MFA data)
     * @return a {@link DTOResponseToken} containing the access token, refresh token,
     *         and associated roles/authorities
     * @throws RuntimeException if authentication fails due to invalid credentials
     *                          or any security constraint violation
     */
    DTOResponseToken login(DTORequestUserAuth dtoRequestUserAuth);

    /**
     * Renews the access token using a valid refresh token.
     *
     * @param dtoRequestToken object containing the previously issued refresh token
     * @return a {@link DTOResponseToken} containing a new access token and,
     *         optionally, a new refresh token depending on the implementation strategy
     * @throws RuntimeException if the refresh token is invalid, expired, or revoked
     */
    DTOResponseToken refresh(DTORequestToken dtoRequestToken);

    /**
     * Terminates the user session by invalidating the provided refresh token.
     *
     * @param refreshToken identifier of the refresh token to be invalidated
     * @return a {@link DTOResponseToken} representing the post-logout state
     *         (typically containing nullified or invalidated token data)
     * @throws RuntimeException if the token is not found, already invalidated,
     *                          or cannot be processed
     */
    DTOResponseToken logout(UUID refreshToken);
}
