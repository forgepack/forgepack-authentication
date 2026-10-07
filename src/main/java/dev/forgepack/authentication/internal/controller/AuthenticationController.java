package dev.forgepack.authentication.internal.controller;

import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
import dev.forgepack.authentication.internal.service.AuthenticationServiceImpl;
import dev.forgepack.authentication.internal.payload.UserAuthRequest;
import dev.forgepack.authorization.internal.service.UserService;
import dev.forgepack.authorization.internal.payload.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

/**
 * REST controller responsible for authentication operations.
 *
 * <p>Exposes endpoints for login, token refresh, logout, and credential management
 * under the {@code /auth} base path.</p>
 *
 * <h3>Available endpoints</h3>
 * <ul>
 *     <li>{@code POST /auth/login} – authenticates the user and issues access and refresh tokens</li>
 *     <li>{@code POST /auth/refresh} – exchanges a valid refresh token for a new token pair</li>
 *     <li>{@code DELETE /auth/logout/{refreshToken}} – invalidates the given refresh token</li>
 *     <li>{@code PUT /auth/changePassword} – updates the authenticated user's password</li>
 *     <li>{@code PUT /auth/resetPassword} – resets the password for the specified username</li>
 *     <li>{@code PUT /auth/resetSecret} – resets the two-factor secret for the specified username</li>
 * </ul>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 *
 * @see AuthenticationServiceImpl
 * @see UserService
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationServiceImpl serviceAuthenticationImpl;
    private final UserService serviceUser;

    public AuthenticationController(AuthenticationServiceImpl serviceAuthenticationImpl, UserService serviceUser) {
        this.serviceAuthenticationImpl = serviceAuthenticationImpl;
        this.serviceUser = serviceUser;
    }

    /**
     * Authenticates the user and issues a new token pair.
     *
     * @param value the authentication request containing username, password, and two-factor secret
     * @return {@code 200 OK} with the generated {@link TokenResponse}
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid UserAuthRequest value){
        return ResponseEntity.ok().body(serviceAuthenticationImpl.login(value));
    }
    /**
     * Refreshes the token pair using a valid refresh token.
     *
     * @param value the token request containing the current refresh token
     * @return {@code 200 OK} with the new {@link TokenResponse}
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid TokenRequest value){
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.refresh(value));
    }
    /**
     * Invalidates the given refresh token, effectively logging the user out.
     *
     * @param refreshToken the UUID of the refresh token to be revoked
     * @return {@code 202 Accepted} with the invalidated {@link TokenResponse}
     */
    @DeleteMapping("/logout/{refreshToken}")
    public ResponseEntity<TokenResponse> logout(@PathVariable("refreshToken") UUID refreshToken) {
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.logout(refreshToken));
    }
    /**
     * Changes the password of the authenticated user.
     *
     * @param updated the request containing the username and new password
     * @return {@code 202 Accepted} with the updated {@link UserResponse}
     */
    @PutMapping("/changePassword")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<UserResponse> changePassword(@RequestBody @Valid UserAuthRequest updated){
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.changePassword(updated));
    }
    /**
     * Resets the password of the user identified by the provided username.
     *
     * @param updated the request containing the target username
     * @return {@code 202 Accepted} with the updated {@link UserResponse}
     */
    @PutMapping("/resetPassword")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER', 'VIEWER')")
    public ResponseEntity<UserResponse> resetPassword(@RequestBody UserAuthRequest updated) {
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.resetPassword(updated.username()));
    }
    /**
     * Resets the two-factor authentication secret of the user identified by the provided username.
     *
     * @param updated the request containing the target username
     * @return {@code 202 Accepted} with the updated {@link UserResponse}
     */
    @PutMapping("/resetSecret")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER', 'VIEWER')")
    public ResponseEntity<UserResponse> resetSecret(@RequestBody UserAuthRequest updated) {
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.resetSecret(updated.username()));
    }
}
