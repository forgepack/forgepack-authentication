package dev.forgepack.authentication.internal.service;

import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.authentication.api.service.AuthenticationService;
import dev.forgepack.authentication.internal.configuration.JwtConfiguration;
// import dev.forgepack.security.internal.utils.Information;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authorization.internal.model.Role;
import dev.forgepack.authorization.internal.model.User;
import dev.forgepack.authorization.internal.payload.UserRequest;
import dev.forgepack.authorization.internal.payload.UserResponse;
import dev.forgepack.authorization.internal.repository.RoleRepository;
import dev.forgepack.authorization.internal.repository.UserRepository;
// import dev.forgepack.authorization.internal.service.ServiceCustomUserDetails;
import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.UserAuthRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
import dev.forgepack.authentication.internal.repository.TokenRepository;
import dev.forgepack.utils.api.service.EncryptorService;
import dev.forgepack.utils.internal.service.EmailServiceImpl;
import dev.forgepack.utils.internal.service.EncryptorServiceImpl;
import dev.forgepack.utils.internal.utils.QRCode;
import dev.forgepack.validation.api.service.UniqueCheckableService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthenticationServiceImpl implements UniqueCheckableService, AuthenticationService {

    //    private final ServiceRecaptcha serviceRecaptcha;
    private final EncryptorService encryptorService;
    private final AuthenticationManager authenticationManager;
    private final JwtConfiguration jwtConfiguration;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository repositoryRole;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;
    private final Mapper<Token, TokenRequest, TokenResponse> mapperToken;
    private final Mapper<User, UserRequest, UserResponse> mapperUser;
    private final CustomUserDetailsService customUserDetailsService;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    public AuthenticationServiceImpl(EncryptorService encryptorService, AuthenticationManager authenticationManager, JwtConfiguration jwtConfiguration, TokenRepository tokenRepository, UserRepository userRepository, RoleRepository repositoryRole, PasswordEncoder passwordEncoder, EmailServiceImpl emailService, Mapper<Token, TokenRequest, TokenResponse> mapperToken, Mapper<User, UserRequest, UserResponse> mapperUser, CustomUserDetailsService customUserDetailsService) {
        this.encryptorService = encryptorService;
        this.authenticationManager = authenticationManager;
        this.jwtConfiguration = jwtConfiguration;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.repositoryRole = repositoryRole;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.mapperToken = mapperToken;
        this.mapperUser = mapperUser;
        this.customUserDetailsService = customUserDetailsService;
    }
    
    @Override
    public TokenResponse login(UserAuthRequest userAuthRequest) {
        try {
//            captchaTest(dtoRequestUserAuth.getCaptchaToken());
            validateTOTP(userAuthRequest.username(), userAuthRequest.secret());
            customUserDetailsService.loadUserByUsername(userAuthRequest.username());
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userAuthRequest.username(), userAuthRequest.password()));
            resetAttempts(userAuthRequest.username());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtConfiguration.generateToken(authentication.getName());
            UUID refreshToken = UUID.randomUUID();
            tokenRepository.save(new Token(refreshToken, true));
            return new TokenResponse(
                    token,
                    refreshToken,
                    authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())
            );
        } catch (BadCredentialsException e) {
            addAttempt(userAuthRequest);
            throw e;
        }
    }
    // @Override
    public UserResponse signup(UserRequest created){
        User user = mapperUser.toEntity(created);
        String password = generateSecurePassword();
        String secret = generateSecret();
        try {
            user.setPassword(passwordEncoder.encode(password));
            user.setSecret(encryptorService.encrypt(secret));
            Set<Role> roles = new HashSet<>();
            roles.add(repositoryRole.findByName("VIEWER").orElseThrow(() -> new RuntimeException("Default role VIEWER not found")));
            user.setRole(roles);
            user.setActive(true);
            user.setAttempt(0);
            byte[] qrCodeBytes = QRCode.generateQRCodeBytes(buildSecretUri(user.getUsername(), user.getSecret()), 200);
            String emailContent = emailService.buildWelcomeEmailContent(user.getUsername(), password, secret);
            emailService.sendHtmlMessageWithAttachment(user.getEmail(), "Account Created", emailContent, qrCodeBytes, "qrcode.png", "image/png");
        } catch (MailException e) {
            log.error("Error sending email for {}: {}", user.getUsername(), e.getMessage());
            throw new BadCredentialsException("Failed to send welcome email");
        } catch (Exception e) {
            log.error("Error generating TOTP secret for {}: {}", created, e.getMessage(), e);
            throw new BadCredentialsException("Invalid secret");
        }
        // log.info("{} creating a new user", Information.getCurrentUser().orElse("Unknown User"));
        return mapperUser.toResponse(userRepository.save(user));
    }
    public UserResponse resetPassword(String username) {
        User user = isValidToChange(username);
        String password = generateSecurePassword();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        try {
            emailService.sendSimpleMessage(user.getEmail(), "Password Reset",
                    "Hello " + user.getUsername() + ",\n\nYour password has been reset. Your new temporary password is:\n\n" + password + "\n\nPlease change it after logging in.");
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
        // log.info("{} reset password for user with ID: {}", Information.getCurrentUser().orElse("Unknown User"), user.getId());
        return mapperUser.toResponse(user);
    }
    public UserResponse changePassword(UserAuthRequest updated){
        User user = isValidToChange(updated.id());
        Objects.requireNonNull(user).setPassword(passwordEncoder.encode(updated.password()));
        userRepository.save(user);
        // log.info("{} changing user password with ID: {}", Information.getCurrentUser().orElse("Unknown User"), user.getId());
        return mapperUser.toResponse(user);
    }
    public UserResponse resetSecret(String username) {
        User user = isValidToChange(username);
        String secret = generateSecret();
        try {
            user.setSecret(encryptorService.encrypt(secret));
            userRepository.save(user);
            byte[] qrCodeBytes = QRCode.generateQRCodeBytes(buildSecretUri(user.getUsername(), user.getSecret()), 200);
            String emailContent = emailService.buildWelcomeEmailContent(user.getUsername(), "Your password is the same as before", secret);
            emailService.sendHtmlMessageWithAttachment(user.getEmail(), "Reset TOTP requested", emailContent, qrCodeBytes, "qrcode.png", "image/png");
            // log.info("{} resetting user secret with ID: {}", Information.getCurrentUser().orElse("Unknown User"), user.getId());
            return mapperUser.toResponse(user);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reset TOTP for user: " + user.getUsername());
        }
    }
    @Override
    public TokenResponse refresh(TokenRequest tokenRequest) {
        if (tokenRepository.existsByRefreshToken(tokenRequest.refreshToken()) &&
                jwtConfiguration.validateJwt(tokenRequest.accessToken())) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(
                    jwtConfiguration.getUsernameFromJwt(tokenRequest.accessToken())
            );
            String tokenResponse = jwtConfiguration.generateToken(jwtConfiguration.getUsernameFromJwt(tokenRequest.accessToken()));
            Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            return new TokenResponse(tokenResponse, tokenRequest.refreshToken(), roles);
        } else {
            logout(tokenRequest.refreshToken());
            throw new BadCredentialsException("Invalid or expired token. Please log in again.");
        }
    }
    @Override
    public TokenResponse logout(UUID refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken)
                .map(token -> {
                    tokenRepository.deleteById(token.getId());
                    return mapperToken.toResponse(token);
                })
                .orElseThrow(() ->
                        new EntityNotFoundException("Token not found.")
                );
    }
    //    @Override
//    public void captchaTest(String captchaToken) {
//        if (!serviceRecaptcha.validateCaptcha(captchaToken)) {
//            log.error("Invalid or suspicious CAPTCHA: {}", captchaToken);
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or suspicious CAPTCHA");
//        }
//    }
    public void addAttempt(UserAuthRequest userAuthRequest) {
        User entity = userRepository.findByUsername(userAuthRequest.username()).orElseThrow(() -> new RuntimeException("Resource not found"));
        entity.setAttempt(entity.getAttempt() == null ? 0 : entity.getAttempt() + 1);
        if(entity.getAttempt() > 4) {
            entity.setActive(false);
            userRepository.save(entity);
            throw new DisabledException("User account has been disabled due to too many failed login attempts.");
        }
        userRepository.save(entity);
    }
    public void resetAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAttempt(0);
            userRepository.save(user);
        });
    }
    public void validateTOTP(String userName, Integer secretKey) {
        log.info("Validating TOTP for user: {}", userName);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new BadCredentialsException("User not found"));
        String secret = user.getSecret();
        try {
            secret = encryptorService.decrypt(secret);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid secret");
        }
        if (StringUtils.hasText(secret)) {
            if (secretKey != null) {
                try {
                    if (!verifyCode(secret, secretKey, 1)) {
//                        log.warn("TOTP code {} was not valid for user {}", secretKey, userName);
                        throw new BadCredentialsException("Invalid TOTP code");
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                    throw new InternalAuthenticationServiceException("Failed to verify TOTP code due to cryptographic error", e);
                }
            } else {
//                throw new MissingTOTPKeyAuthenticatorException("TOTP code is mandatory");
                throw new BadCredentialsException("TOTP code is mandatory");
            }
        }
    }
    public boolean verifyCode(String secret, int code, int variance)
            throws InvalidKeyException, NoSuchAlgorithmException {
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        byte[] secretBytes = new Base32().decode(secret);
        for (int i = -variance; i <= variance; i++) {
            long calculatedCode = getCode(secretBytes, timeIndex + i);
            if (calculatedCode == code) {
                return true;
            }
        }
        return false;
    }
    private long getCode(byte[] secret, long timeIndex) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret, "HmacSHA1"));
        ByteBuffer buffer = ByteBuffer.allocate(8).putLong(timeIndex);
        byte[] hash = mac.doFinal(buffer.array());
        int offset = hash[19] & 0xf;
        long truncatedHash = hash[offset] & 0x7f;
        for (int i = 1; i < 4; i++) {
            truncatedHash <<= 8;
            truncatedHash |= hash[offset + i] & 0xff;
        }
        return truncatedHash % 1000000;
    }
    public String generateSecret() {
        byte[] buffer = new byte[10];
        new SecureRandom().nextBytes(buffer);
        return new String(new Base32().encode(buffer));
    }
    public String buildSecretUri(String username, String secret) throws Exception {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                username,
                username + "@forgepack.dev",
                encryptorService.decrypt(secret),
                "Forgepack"
        );
    }
    public String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+[]{}|;:,.<>?";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        String allChars = upper + lower + digits + special;
        for (int i = 4; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    public User isValidToChange(UUID id) {
        // String currentUser = Information.getCurrentUser().orElse("Unknown User");
        User user = userRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        // User userCurrent = userRepository.findByUsername(currentUser).orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        // if ((userCurrent.getUsername() != null && user.getUsername() != null &&
        //         userCurrent.getUsername().equals(user.getUsername())) ||
        //         userCurrent.getRole().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
        //     return user;
        // } else {
            // log.warn("{} attempted unauthorized access to user with ID: {}", currentUser, id);
            throw new EntityNotFoundException("Resource not found");
        // }
    }
    public User isValidToChange(String username) {
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        // String currentUsername = Information.getCurrentUser().orElse(null);
        // if (currentUsername == null) {
        //     return user;
        // }
        // User currentUser = userRepository.findByUsername(currentUsername)
        //         .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        // boolean isSameUser = currentUser.getUsername().equalsIgnoreCase(user.getUsername());
        // boolean isAdmin   = currentUser.getRole().stream().anyMatch(role -> role.getName().equals("ADMIN"));
        // if (isSameUser || isAdmin) {
        //     return user;
        // }
        // log.warn("{} attempted unauthorized access to user with username: {}", currentUsername, username);
        throw new EntityNotFoundException("Resource not found");
    }
    @Override
    public boolean existsByField(String field, Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsByField'");
    }
    @Override
    public boolean existsByFieldAndIdNot(String field, Object value, UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsByFieldAndIdNot'");
    }
}
