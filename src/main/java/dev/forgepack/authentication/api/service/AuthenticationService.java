package dev.forgepack.authentication.api.service;

import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.UserAuthRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
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
public interface AuthenticationService {

    /**
     * Authenticates a user based on the provided credentials.
     *
     * @param userAuthRequest object containing user authentication credentials
     *                           (e.g., username and password, optionally including MFA data)
     * @return a {@link TokenResponse} containing the access token, refresh token,
     *         and associated roles/authorities
     * @throws RuntimeException if authentication fails due to invalid credentials
     *                          or any security constraint violation
     */
    TokenResponse login(UserAuthRequest userAuthRequest);

    /**
     * Renews the access token using a valid refresh token.
     *
     * @param tokenRequest object containing the previously issued refresh token
     * @return a {@link TokenResponse} containing a new access token and,
     *         optionally, a new refresh token depending on the implementation strategy
     * @throws RuntimeException if the refresh token is invalid, expired, or revoked
     */
    TokenResponse refresh(TokenRequest tokenRequest);

    /**
     * Terminates the user session by invalidating the provided refresh token.
     *
     * @param refreshToken identifier of the refresh token to be invalidated
     * @return a {@link TokenResponse} representing the post-logout state
     *         (typically containing nullified or invalidated token data)
     * @throws RuntimeException if the token is not found, already invalidated,
     *                          or cannot be processed
     */
    TokenResponse logout(UUID refreshToken);
}
