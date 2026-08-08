package dev.forgepack.authentication.internal.controller;

import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import dev.forgepack.authorization.internal.payload.DTOResponseUser;
import dev.forgepack.authorization.internal.payload.DTORequestUserAuth;
import dev.forgepack.authorization.internal.service.ServiceAuthenticationImpl;
import dev.forgepack.authorization.internal.service.ServiceUser;
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
 * @see ServiceAuthenticationImpl
 * @see ServiceUser
 */
@RestController
@RequestMapping("/auth")
public class ControllerAuthentication {

    private final ServiceAuthenticationImpl serviceAuthenticationImpl;
    private final ServiceUser serviceUser;

    public ControllerAuthentication(ServiceAuthenticationImpl serviceAuthenticationImpl, ServiceUser serviceUser) {
        this.serviceAuthenticationImpl = serviceAuthenticationImpl;
        this.serviceUser = serviceUser;
    }

    /**
     * Authenticates the user and issues a new token pair.
     *
     * @param value the authentication request containing username, password, and two-factor secret
     * @return {@code 200 OK} with the generated {@link DTOResponseToken}
     */
    @PostMapping("/login")
    public ResponseEntity<DTOResponseToken> login(@RequestBody @Valid DTORequestUserAuth value){
        return ResponseEntity.ok().body(serviceAuthenticationImpl.login(value));
    }
    /**
     * Refreshes the token pair using a valid refresh token.
     *
     * @param value the token request containing the current refresh token
     * @return {@code 200 OK} with the new {@link DTOResponseToken}
     */
    @PostMapping("/refresh")
    public ResponseEntity<DTOResponseToken> refresh(@RequestBody @Valid DTORequestToken value){
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.refresh(value));
    }
    /**
     * Invalidates the given refresh token, effectively logging the user out.
     *
     * @param refreshToken the UUID of the refresh token to be revoked
     * @return {@code 202 Accepted} with the invalidated {@link DTOResponseToken}
     */
    @DeleteMapping("/logout/{refreshToken}")
    public ResponseEntity<DTOResponseToken> logout(@PathVariable("refreshToken") UUID refreshToken) {
        return ResponseEntity.accepted().body(serviceAuthenticationImpl.logout(refreshToken));
    }
    /**
     * Changes the password of the authenticated user.
     *
     * @param updated the request containing the username and new password
     * @return {@code 202 Accepted} with the updated {@link DTOResponseUser}
     */
    @PutMapping("/changePassword")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<DTOResponseUser> changePassword(@RequestBody @Valid DTORequestUserAuth updated){
        return ResponseEntity.accepted().body(serviceUser.changePassword(updated));
    }
    /**
     * Resets the password of the user identified by the provided username.
     *
     * @param updated the request containing the target username
     * @return {@code 202 Accepted} with the updated {@link DTOResponseUser}
     */
    @PutMapping("/resetPassword")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER', 'VIEWER')")
    public ResponseEntity<DTOResponseUser> resetPassword(@RequestBody DTORequestUserAuth updated) {
        return ResponseEntity.accepted().body(serviceUser.resetPassword(updated.username()));
    }
    /**
     * Resets the two-factor authentication secret of the user identified by the provided username.
     *
     * @param updated the request containing the target username
     * @return {@code 202 Accepted} with the updated {@link DTOResponseUser}
     */
    @PutMapping("/resetSecret")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER', 'VIEWER')")
    public ResponseEntity<DTOResponseUser> resetSecret(@RequestBody DTORequestUserAuth updated) {
        return ResponseEntity.accepted().body(serviceUser.resetSecret(updated.username()));
    }
}
