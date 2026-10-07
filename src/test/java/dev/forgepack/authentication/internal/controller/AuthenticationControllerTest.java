package dev.forgepack.authentication.internal.controller;

import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.UserAuthRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
import dev.forgepack.authentication.internal.service.AuthenticationServiceImpl;
import dev.forgepack.authorization.internal.payload.UserResponse;
import dev.forgepack.authorization.internal.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock private AuthenticationServiceImpl serviceAuthenticationImpl;
    @Mock private UserService serviceUser;

    private AuthenticationController controller;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        controller = new AuthenticationController(serviceAuthenticationImpl, serviceUser);
    }

    @Test
    void login_returnsOkWithBody() {
        UserAuthRequest request = new UserAuthRequest(null, "john", "Password1!", 123456);
        TokenResponse expected = new TokenResponse("token", UUID.randomUUID(), Set.of("ROLE_USER"));
        when(serviceAuthenticationImpl.login(request)).thenReturn(expected);

        ResponseEntity<TokenResponse> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void refresh_returnsAcceptedWithBody() {
        TokenRequest request = new TokenRequest(null, "access-token", UUID.randomUUID());
        TokenResponse expected = new TokenResponse(UUID.randomUUID());
        when(serviceAuthenticationImpl.refresh(request)).thenReturn(expected);

        ResponseEntity<TokenResponse> response = controller.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void logout_returnsAcceptedWithBody() {
        UUID refreshToken = UUID.randomUUID();
        TokenResponse expected = new TokenResponse(refreshToken);
        when(serviceAuthenticationImpl.logout(refreshToken)).thenReturn(expected);

        ResponseEntity<TokenResponse> response = controller.logout(refreshToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void changePassword_returnsAcceptedWithBody() {
        UserAuthRequest request = new UserAuthRequest(UUID.randomUUID(), "john", "NewPassword1!", 123456);
        UserResponse expected = new UserResponse(UUID.randomUUID(), "john", "john@forgepack.dev", 0, true, Set.of());
        when(serviceAuthenticationImpl.changePassword(request)).thenReturn(expected);

        ResponseEntity<UserResponse> response = controller.changePassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void resetPassword_returnsAcceptedWithBody() {
        UserAuthRequest request = new UserAuthRequest(null, "john", "Password1!", 123456);
        UserResponse expected = new UserResponse(UUID.randomUUID(), "john", "john@forgepack.dev", 0, true, Set.of());
        when(serviceAuthenticationImpl.resetPassword("john")).thenReturn(expected);

        ResponseEntity<UserResponse> response = controller.resetPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void resetSecret_returnsAcceptedWithBody() {
        UserAuthRequest request = new UserAuthRequest(null, "john", "Password1!", 123456);
        UserResponse expected = new UserResponse(UUID.randomUUID(), "john", "john@forgepack.dev", 0, true, Set.of());
        when(serviceAuthenticationImpl.resetSecret("john")).thenReturn(expected);

        ResponseEntity<UserResponse> response = controller.resetSecret(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }
}
