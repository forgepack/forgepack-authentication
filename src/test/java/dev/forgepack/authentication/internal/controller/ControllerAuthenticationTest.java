package dev.forgepack.authentication.internal.controller;

import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTORequestUserAuth;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import dev.forgepack.authentication.internal.service.ServiceAuthenticationImpl;
import dev.forgepack.authorization.internal.payload.DTOResponseUser;
import dev.forgepack.authorization.internal.service.ServiceUser;
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
class ControllerAuthenticationTest {

    @Mock private ServiceAuthenticationImpl serviceAuthenticationImpl;
    @Mock private ServiceUser serviceUser;

    private ControllerAuthentication controller;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        controller = new ControllerAuthentication(serviceAuthenticationImpl, serviceUser);
    }

    @Test
    void login_returnsOkWithBody() {
        DTORequestUserAuth request = new DTORequestUserAuth(null, "john", "Password1!", 123456);
        DTOResponseToken expected = new DTOResponseToken("token", UUID.randomUUID(), Set.of("ROLE_USER"));
        when(serviceAuthenticationImpl.login(request)).thenReturn(expected);

        ResponseEntity<DTOResponseToken> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void refresh_returnsAcceptedWithBody() {
        DTORequestToken request = new DTORequestToken(null, "access-token", UUID.randomUUID());
        DTOResponseToken expected = new DTOResponseToken(UUID.randomUUID());
        when(serviceAuthenticationImpl.refresh(request)).thenReturn(expected);

        ResponseEntity<DTOResponseToken> response = controller.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void logout_returnsAcceptedWithBody() {
        UUID refreshToken = UUID.randomUUID();
        DTOResponseToken expected = new DTOResponseToken(refreshToken);
        when(serviceAuthenticationImpl.logout(refreshToken)).thenReturn(expected);

        ResponseEntity<DTOResponseToken> response = controller.logout(refreshToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void changePassword_returnsAcceptedWithBody() {
        DTORequestUserAuth request = new DTORequestUserAuth(UUID.randomUUID(), "john", "NewPassword1!", 123456);
        DTOResponseUser expected = new DTOResponseUser(UUID.randomUUID(), "john", "john@forgepack.dev", 0, true, Set.of());
        when(serviceAuthenticationImpl.changePassword(request)).thenReturn(expected);

        ResponseEntity<DTOResponseUser> response = controller.changePassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void resetPassword_returnsAcceptedWithBody() {
        DTORequestUserAuth request = new DTORequestUserAuth(null, "john", "Password1!", 123456);
        DTOResponseUser expected = new DTOResponseUser(UUID.randomUUID(), "john", "john@forgepack.dev", 0, true, Set.of());
        when(serviceAuthenticationImpl.resetPassword("john")).thenReturn(expected);

        ResponseEntity<DTOResponseUser> response = controller.resetPassword(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void resetSecret_returnsAcceptedWithBody() {
        DTORequestUserAuth request = new DTORequestUserAuth(null, "john", "Password1!", 123456);
        DTOResponseUser expected = new DTOResponseUser(UUID.randomUUID(), "john", "john@forgepack.dev", 0, true, Set.of());
        when(serviceAuthenticationImpl.resetSecret("john")).thenReturn(expected);

        ResponseEntity<DTOResponseUser> response = controller.resetSecret(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
    }
}
