package dev.forgepack.authentication.internal.service;

import dev.forgepack.authentication.internal.configuration.JwtConfiguration;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTORequestUserAuth;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import dev.forgepack.authentication.internal.repository.RepositoryToken;
import dev.forgepack.authorization.internal.model.Role;
import dev.forgepack.authorization.internal.model.User;
import dev.forgepack.authorization.internal.payload.DTORequestUser;
import dev.forgepack.authorization.internal.payload.DTOResponseUser;
import dev.forgepack.authorization.internal.repository.RepositoryRole;
import dev.forgepack.authorization.internal.repository.RepositoryUser;
import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.utils.internal.service.ServiceEmailImpl;
import dev.forgepack.utils.internal.utils.E2EE;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAuthenticationImplTest {

    private static final String TOTP_SECRET = "JBSWY3DPEHPK3PXP";

    @Mock private E2EE e2EE;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtConfiguration jwtConfiguration;
    @Mock private RepositoryToken repositoryToken;
    @Mock private RepositoryUser repositoryUser;
    @Mock private RepositoryRole repositoryRole;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ServiceEmailImpl serviceEmail;
    @Mock private Mapper<Token, DTORequestToken, DTOResponseToken> mapperToken;
    @Mock private Mapper<User, DTORequestUser, DTOResponseUser> mapperUser;
    @Mock private ServiceCustomUserDetails serviceCustomUserDetails;

    private ServiceAuthenticationImpl service;

    @BeforeEach
    void setUp() {
        service = new ServiceAuthenticationImpl(e2EE, authenticationManager, jwtConfiguration, repositoryToken,
                repositoryUser, repositoryRole, passwordEncoder, serviceEmail, mapperToken, mapperUser,
                serviceCustomUserDetails);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static int currentTotp(String secret) throws Exception {
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        byte[] secretBytes = new Base32().decode(secret);
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secretBytes, "HmacSHA1"));
        ByteBuffer buffer = ByteBuffer.allocate(8).putLong(timeIndex);
        byte[] hash = mac.doFinal(buffer.array());
        int offset = hash[19] & 0xf;
        long truncatedHash = hash[offset] & 0x7f;
        for (int i = 1; i < 4; i++) {
            truncatedHash <<= 8;
            truncatedHash |= hash[offset + i] & 0xff;
        }
        return (int) (truncatedHash % 1000000);
    }

    private User buildUser(String username, Integer attempt, Boolean active) {
        return new User(username, username + "@forgepack.dev", "encodedPassword", attempt, active, "encryptedSecret", new HashSet<>());
    }

    @Test
    void login_success() throws Exception {
        int totp = currentTotp(TOTP_SECRET);
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn(TOTP_SECRET);
        UserDetails userDetails = mock(UserDetails.class);
        when(serviceCustomUserDetails.loadUserByUsername("john")).thenReturn(userDetails);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john");
        doReturnAuthorities(authentication, "ROLE_VIEWER");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtConfiguration.generateToken("john")).thenReturn("token123");

        DTORequestUserAuth request = new DTORequestUserAuth(null, "john", "Password1!", totp);
        DTOResponseToken response = service.login(request);

        assertThat(response.getAccessToken()).isEqualTo("token123");
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getRole()).containsExactly("ROLE_VIEWER");
        verify(repositoryToken, times(1)).save(any(Token.class));
        assertThat(user.getAttempt()).isZero();
    }

    private void doReturnAuthorities(Authentication authentication, String authority) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
        org.mockito.Mockito.doReturn(authorities).when(authentication).getAuthorities();
    }

    @Test
    void login_invalidCredentials_incrementsAttemptAndRethrows() throws Exception {
        int totp = currentTotp(TOTP_SECRET);
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn(TOTP_SECRET);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        DTORequestUserAuth request = new DTORequestUserAuth(null, "john", "Password1!", totp);

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("bad creds");

        assertThat(user.getAttempt()).isEqualTo(1);
        verify(repositoryUser, times(1)).save(user);
    }

    @Test
    void login_tooManyAttempts_disablesAccount() throws Exception {
        int totp = currentTotp(TOTP_SECRET);
        User user = buildUser("john", 4, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn(TOTP_SECRET);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        DTORequestUserAuth request = new DTORequestUserAuth(null, "john", "Password1!", totp);

        assertThatThrownBy(() -> service.login(request)).isInstanceOf(DisabledException.class);

        assertThat(user.getAttempt()).isEqualTo(5);
        assertThat(user.getActive()).isFalse();
    }

    @Test
    void addAttempt_userNotFound_throwsRuntimeException() {
        when(repositoryUser.findByUsername("ghost")).thenReturn(Optional.empty());
        DTORequestUserAuth request = new DTORequestUserAuth(null, "ghost", "Password1!", 123456);
        assertThatThrownBy(() -> service.addAttempt(request)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void resetAttempts_existingUser_resetsToZero() {
        User user = buildUser("john", 3, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        service.resetAttempts("john");
        assertThat(user.getAttempt()).isZero();
        verify(repositoryUser).save(user);
    }

    @Test
    void resetAttempts_unknownUser_doesNothing() {
        when(repositoryUser.findByUsername("ghost")).thenReturn(Optional.empty());
        service.resetAttempts("ghost");
        verify(repositoryUser, never()).save(any());
    }

    @Test
    void validateTOTP_success_doesNotThrow() throws Exception {
        int totp = currentTotp(TOTP_SECRET);
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn(TOTP_SECRET);
        service.validateTOTP("john", totp);
    }

    @Test
    void validateTOTP_wrongCode_throwsBadCredentials() throws Exception {
        int totp = currentTotp(TOTP_SECRET);
        int wrongCode = (totp + 500000) % 1000000;
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn(TOTP_SECRET);
        assertThatThrownBy(() -> service.validateTOTP("john", wrongCode))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid TOTP code");
    }

    @Test
    void validateTOTP_missingCode_throwsMandatory() throws Exception {
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn(TOTP_SECRET);
        assertThatThrownBy(() -> service.validateTOTP("john", null))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("TOTP code is mandatory");
    }

    @Test
    void validateTOTP_blankSecret_skipsValidation() throws Exception {
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenReturn("");
        service.validateTOTP("john", null);
    }

    @Test
    void validateTOTP_userNotFound_throwsBadCredentials() {
        when(repositoryUser.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.validateTOTP("ghost", 123456))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("User not found");
    }

    @Test
    void validateTOTP_decryptFailure_throwsInvalidSecret() throws Exception {
        User user = buildUser("john", 0, true);
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(user));
        when(e2EE.decrypt("encryptedSecret")).thenThrow(new E2EE.E2EEException("boom"));
        assertThatThrownBy(() -> service.validateTOTP("john", 123456))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid secret");
    }

    @Test
    void verifyCode_matchesAndMismatches() throws Exception {
        int totp = currentTotp(TOTP_SECRET);
        assertThat(service.verifyCode(TOTP_SECRET, totp, 1)).isTrue();
        int wrongCode = (totp + 500000) % 1000000;
        assertThat(service.verifyCode(TOTP_SECRET, wrongCode, 1)).isFalse();
    }

    @Test
    void generateSecret_returnsDecodableBase32() {
        String secret = service.generateSecret();
        assertThat(secret).isNotBlank();
        assertThat(new Base32().decode(secret)).hasSize(10);
    }

    @Test
    void generateSecurePassword_meetsComplexityRules() {
        String password = service.generateSecurePassword();
        assertThat(password).hasSize(8);
        assertThat(password).containsPattern("[A-Z]");
        assertThat(password).containsPattern("[a-z]");
        assertThat(password).containsPattern("[0-9]");
        assertThat(password).containsPattern("[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?]");
    }

    @Test
    void buildSecretUri_returnsExpectedFormat() throws Exception {
        when(e2EE.decrypt("enc")).thenReturn("PLAINSECRET");
        String uri = service.buildSecretUri("alice", "enc");
        assertThat(uri).isEqualTo("otpauth://totp/alice:alice@forgepack.dev?secret=PLAINSECRET&issuer=Forgepack");
    }

    @Test
    void refresh_success_returnsNewToken() {
        UUID refreshToken = UUID.randomUUID();
        DTORequestToken request = new DTORequestToken(null, "access-token", refreshToken);
        when(repositoryToken.existsByRefreshToken(refreshToken)).thenReturn(true);
        when(jwtConfiguration.validateJwt("access-token")).thenReturn(true);
        when(jwtConfiguration.getUsernameFromJwt("access-token")).thenReturn("john");
        UserDetails userDetails = mock(UserDetails.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        org.mockito.Mockito.doReturn(authorities).when(userDetails).getAuthorities();
        when(serviceCustomUserDetails.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtConfiguration.generateToken("john")).thenReturn("new-access-token");

        DTOResponseToken response = service.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getRole()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void refresh_invalidToken_logsOutAndThrows() {
        UUID refreshToken = UUID.randomUUID();
        DTORequestToken request = new DTORequestToken(null, "access-token", refreshToken);
        when(repositoryToken.existsByRefreshToken(refreshToken)).thenReturn(true);
        when(jwtConfiguration.validateJwt("access-token")).thenReturn(false);
        Token token = new Token(refreshToken, true);
        when(repositoryToken.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));
        when(mapperToken.toResponse(token)).thenReturn(new DTOResponseToken(refreshToken));

        assertThatThrownBy(() -> service.refresh(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired token. Please log in again.");

        verify(repositoryToken).deleteById(token.getId());
    }

    @Test
    void logout_success_deletesToken() {
        UUID refreshToken = UUID.randomUUID();
        Token token = new Token(refreshToken, true);
        DTOResponseToken expected = new DTOResponseToken(refreshToken);
        when(repositoryToken.findByRefreshToken(refreshToken)).thenReturn(Optional.of(token));
        when(mapperToken.toResponse(token)).thenReturn(expected);

        DTOResponseToken result = service.logout(refreshToken);

        assertThat(result).isEqualTo(expected);
        verify(repositoryToken).deleteById(token.getId());
    }

    @Test
    void logout_tokenNotFound_throwsEntityNotFound() {
        UUID refreshToken = UUID.randomUUID();
        when(repositoryToken.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.logout(refreshToken));
    }

    @Test
    void isValidToChangeById_alwaysThrowsEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repositoryUser.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(buildUser("john", 0, true)));
        assertThrows(EntityNotFoundException.class, () -> service.isValidToChange(id));
    }

    @Test
    void isValidToChangeByUsername_alwaysThrowsEntityNotFound() {
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(buildUser("john", 0, true)));
        assertThrows(EntityNotFoundException.class, () -> service.isValidToChange("john"));
    }

    @Test
    void changePassword_throwsEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repositoryUser.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(buildUser("john", 0, true)));
        DTORequestUserAuth updated = new DTORequestUserAuth(id, "john", "NewPassword1!", 111111);
        assertThrows(EntityNotFoundException.class, () -> service.changePassword(updated));
    }

    @Test
    void resetPassword_throwsEntityNotFound() {
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(buildUser("john", 0, true)));
        assertThrows(EntityNotFoundException.class, () -> service.resetPassword("john"));
    }

    @Test
    void resetSecret_throwsEntityNotFound() {
        when(repositoryUser.findByUsername("john")).thenReturn(Optional.of(buildUser("john", 0, true)));
        assertThrows(EntityNotFoundException.class, () -> service.resetSecret("john"));
    }

    @Test
    void signup_success_sendsWelcomeEmail() throws Exception {
        DTORequestUser dtoRequestUser = new DTORequestUser(null, "alice", "alice@forgepack.dev", Set.of());
        User newUser = new User("alice", "alice@forgepack.dev", null, null, null, null, new HashSet<>());
        when(mapperUser.toEntity(dtoRequestUser)).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
        when(e2EE.encrypt(anyString())).thenReturn("encryptedSecret");
        when(e2EE.decrypt("encryptedSecret")).thenReturn("PLAINSECRET");
        when(repositoryRole.findByName("VIEWER")).thenReturn(Optional.of(new Role("VIEWER", Set.of())));
        when(serviceEmail.buildWelcomeEmailContent(anyString(), anyString(), anyString())).thenReturn("content");
        when(repositoryUser.save(newUser)).thenReturn(newUser);
        DTOResponseUser expected = new DTOResponseUser(UUID.randomUUID(), "alice", "alice@forgepack.dev", 0, true, Set.of());
        when(mapperUser.toResponse(newUser)).thenReturn(expected);

        DTOResponseUser result = service.signup(dtoRequestUser);

        assertThat(result).isEqualTo(expected);
        assertThat(newUser.getActive()).isTrue();
        assertThat(newUser.getAttempt()).isZero();
        verify(serviceEmail).sendHtmlMessageWithAttachment(eq("alice@forgepack.dev"), eq("Account Created"), eq("content"), any(byte[].class), eq("qrcode.png"), eq("image/png"));
    }

    @Test
    void signup_mailFailure_throwsBadCredentials() throws Exception {
        DTORequestUser dtoRequestUser = new DTORequestUser(null, "alice", "alice@forgepack.dev", Set.of());
        User newUser = new User("alice", "alice@forgepack.dev", null, null, null, null, new HashSet<>());
        when(mapperUser.toEntity(dtoRequestUser)).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
        when(e2EE.encrypt(anyString())).thenReturn("encryptedSecret");
        when(e2EE.decrypt("encryptedSecret")).thenReturn("PLAINSECRET");
        when(repositoryRole.findByName("VIEWER")).thenReturn(Optional.of(new Role("VIEWER", Set.of())));
        when(serviceEmail.buildWelcomeEmailContent(anyString(), anyString(), anyString())).thenReturn("content");
        doThrow(new MailSendException("smtp down")).when(serviceEmail)
                .sendHtmlMessageWithAttachment(anyString(), anyString(), anyString(), any(byte[].class), anyString(), anyString());

        assertThatThrownBy(() -> service.signup(dtoRequestUser))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Failed to send welcome email");
    }

    @Test
    void signup_secretGenerationFailure_throwsBadCredentials() throws Exception {
        DTORequestUser dtoRequestUser = new DTORequestUser(null, "alice", "alice@forgepack.dev", Set.of());
        User newUser = new User("alice", "alice@forgepack.dev", null, null, null, null, new HashSet<>());
        when(mapperUser.toEntity(dtoRequestUser)).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPwd");
        when(e2EE.encrypt(anyString())).thenThrow(new E2EE.E2EEException("boom"));

        assertThatThrownBy(() -> service.signup(dtoRequestUser))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid secret");
    }

    @Test
    void existsByField_throwsUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> service.existsByField("username", "john"));
    }

    @Test
    void existsByFieldAndIdNot_throwsUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> service.existsByFieldAndIdNot("username", "john", UUID.randomUUID()));
    }
}
