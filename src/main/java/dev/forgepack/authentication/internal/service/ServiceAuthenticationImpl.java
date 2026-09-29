package dev.forgepack.authentication.internal.service;

import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.core.api.repository.RepositoryCrud;
import dev.forgepack.core.internal.service.ServiceCrudRestorableImpl;
import dev.forgepack.authentication.api.service.ServiceAuthentication;
import dev.forgepack.authentication.internal.configuration.JwtConfiguration;
// import dev.forgepack.security.internal.utils.Information;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authorization.internal.model.Role;
import dev.forgepack.authorization.internal.model.User;
import dev.forgepack.authorization.internal.payload.DTORequestUser;
import dev.forgepack.authorization.internal.payload.DTOResponseUser;
import dev.forgepack.authorization.internal.repository.RepositoryRole;
import dev.forgepack.authorization.internal.repository.RepositoryUser;
// import dev.forgepack.authorization.internal.service.ServiceCustomUserDetails;
import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTORequestUserAuth;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import dev.forgepack.authentication.internal.repository.RepositoryToken;
import dev.forgepack.utils.internal.service.ServiceEmailImpl;
import dev.forgepack.utils.internal.utils.E2EE;
import dev.forgepack.utils.internal.utils.QRCode;
import dev.forgepack.validation.api.service.ServiceUniqueCheckable;
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
public class ServiceAuthenticationImpl implements ServiceUniqueCheckable, ServiceAuthentication {

    //    private final ServiceRecaptcha serviceRecaptcha;
    private final E2EE e2EE;
    private final AuthenticationManager authenticationManager;
    private final JwtConfiguration jwtConfiguration;
    private final RepositoryToken repositoryToken;
    private final RepositoryUser repositoryUser;
    private final RepositoryRole repositoryRole;
    private final PasswordEncoder passwordEncoder;
    private final ServiceEmailImpl serviceEmail;
    private final Mapper<Token, DTORequestToken, DTOResponseToken> mapperToken;
    private final Mapper<User, DTORequestUser, DTOResponseUser> mapperUser;
    private final ServiceCustomUserDetails serviceCustomUserDetails;
    private static final Logger log = LoggerFactory.getLogger(ServiceAuthenticationImpl.class);

    public ServiceAuthenticationImpl(E2EE e2EE, AuthenticationManager authenticationManager, JwtConfiguration jwtConfiguration, RepositoryToken repositoryToken, RepositoryUser repositoryUser, RepositoryRole repositoryRole, PasswordEncoder passwordEncoder, ServiceEmailImpl serviceEmail,Mapper<Token, DTORequestToken, DTOResponseToken> mapperToken, Mapper<User, DTORequestUser, DTOResponseUser> mapperUser, ServiceCustomUserDetails serviceCustomUserDetails) {
        this.e2EE = e2EE;
        this.authenticationManager = authenticationManager;
        this.jwtConfiguration = jwtConfiguration;
        this.repositoryToken = repositoryToken;
        this.repositoryUser = repositoryUser;
        this.repositoryRole = repositoryRole;
        this.passwordEncoder = passwordEncoder;
        this.serviceEmail = serviceEmail;
        this.mapperToken = mapperToken;
        this.mapperUser = mapperUser;
        this.serviceCustomUserDetails = serviceCustomUserDetails;
    }
    
    @Override
    public DTOResponseToken login(DTORequestUserAuth dtoRequestUserAuth) {
        try {
//            captchaTest(dtoRequestUserAuth.getCaptchaToken());
            validateTOTP(dtoRequestUserAuth.username(), dtoRequestUserAuth.secret());
            serviceCustomUserDetails.loadUserByUsername(dtoRequestUserAuth.username());
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dtoRequestUserAuth.username(), dtoRequestUserAuth.password()));
            resetAttempts(dtoRequestUserAuth.username());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtConfiguration.generateToken(authentication.getName());
            UUID refreshToken = UUID.randomUUID();
            repositoryToken.save(new Token(refreshToken, true));
            return new DTOResponseToken(
                    token,
                    refreshToken,
                    authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())
            );
        } catch (BadCredentialsException e) {
            addAttempt(dtoRequestUserAuth);
            throw e;
        }
    }
    // @Override
    public DTOResponseUser signup(DTORequestUser created){
        User user = mapperUser.toEntity(created);
        String password = generateSecurePassword();
        String secret = generateSecret();
        try {
            user.setPassword(passwordEncoder.encode(password));
            user.setSecret(e2EE.encrypt(secret));
            Set<Role> roles = new HashSet<>();
            roles.add(repositoryRole.findByName("VIEWER").orElseThrow(() -> new RuntimeException("Default role VIEWER not found")));
            user.setRole(roles);
            user.setActive(true);
            user.setAttempt(0);
            byte[] qrCodeBytes = QRCode.generateQRCodeBytes(buildSecretUri(user.getUsername(), user.getSecret()), 200);
            String emailContent = serviceEmail.buildWelcomeEmailContent(user.getUsername(), password, secret);
            serviceEmail.sendHtmlMessageWithAttachment(user.getEmail(), "Account Created", emailContent, qrCodeBytes, "qrcode.png", "image/png");
        } catch (MailException e) {
            log.error("Error sending email for {}: {}", user.getUsername(), e.getMessage());
            throw new BadCredentialsException("Failed to send welcome email");
        } catch (Exception e) {
            log.error("Error generating TOTP secret for {}: {}", created, e.getMessage(), e);
            throw new BadCredentialsException("Invalid secret");
        }
        // log.info("{} creating a new user", Information.getCurrentUser().orElse("Unknown User"));
        return mapperUser.toResponse(repositoryUser.save(user));
    }
    public DTOResponseUser resetPassword(String username) {
        User user = isValidToChange(username);
        String password = generateSecurePassword();
        user.setPassword(passwordEncoder.encode(password));
        repositoryUser.save(user);
        try {
            serviceEmail.sendSimpleMessage(user.getEmail(), "Password Reset",
                    "Hello " + user.getUsername() + ",\n\nYour password has been reset. Your new temporary password is:\n\n" + password + "\n\nPlease change it after logging in.");
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
        // log.info("{} reset password for user with ID: {}", Information.getCurrentUser().orElse("Unknown User"), user.getId());
        return mapperUser.toResponse(user);
    }
    public DTOResponseUser changePassword(DTORequestUserAuth updated){
        User user = isValidToChange(updated.id());
        Objects.requireNonNull(user).setPassword(passwordEncoder.encode(updated.password()));
        repositoryUser.save(user);
        // log.info("{} changing user password with ID: {}", Information.getCurrentUser().orElse("Unknown User"), user.getId());
        return mapperUser.toResponse(user);
    }
    public DTOResponseUser resetSecret(String username) {
        User user = isValidToChange(username);
        String secret = generateSecret();
        try {
            user.setSecret(e2EE.encrypt(secret));
            repositoryUser.save(user);
            byte[] qrCodeBytes = QRCode.generateQRCodeBytes(buildSecretUri(user.getUsername(), user.getSecret()), 200);
            String emailContent = serviceEmail.buildWelcomeEmailContent(user.getUsername(), "Your password is the same as before", secret);
            serviceEmail.sendHtmlMessageWithAttachment(user.getEmail(), "Reset TOTP requested", emailContent, qrCodeBytes, "qrcode.png", "image/png");
            // log.info("{} resetting user secret with ID: {}", Information.getCurrentUser().orElse("Unknown User"), user.getId());
            return mapperUser.toResponse(user);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reset TOTP for user: " + user.getUsername());
        }
    }
    @Override
    public DTOResponseToken refresh(DTORequestToken dtoRequestToken) {
        if (repositoryToken.existsByRefreshToken(dtoRequestToken.refreshToken()) &&
                jwtConfiguration.validateJwt(dtoRequestToken.accessToken())) {
            UserDetails userDetails = serviceCustomUserDetails.loadUserByUsername(
                    jwtConfiguration.getUsernameFromJwt(dtoRequestToken.accessToken())
            );
            String tokenResponse = jwtConfiguration.generateToken(jwtConfiguration.getUsernameFromJwt(dtoRequestToken.accessToken()));
            Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            return new DTOResponseToken(tokenResponse, dtoRequestToken.refreshToken(), roles);
        } else {
            logout(dtoRequestToken.refreshToken());
            throw new BadCredentialsException("Invalid or expired token. Please log in again.");
        }
    }
    @Override
    public DTOResponseToken logout(UUID refreshToken) {
        return repositoryToken.findByRefreshToken(refreshToken)
                .map(token -> {
                    repositoryToken.deleteById(token.getId());
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
    public void addAttempt(DTORequestUserAuth dtoRequestUserAuth) {
        User entity = repositoryUser.findByUsername(dtoRequestUserAuth.username()).orElseThrow(() -> new RuntimeException("Resource not found"));
        entity.setAttempt(entity.getAttempt() == null ? 0 : entity.getAttempt() + 1);
        if(entity.getAttempt() > 4) {
            entity.setActive(false);
            repositoryUser.save(entity);
            throw new DisabledException("User account has been disabled due to too many failed login attempts.");
        }
        repositoryUser.save(entity);
    }
    public void resetAttempts(String username) {
        repositoryUser.findByUsername(username).ifPresent(user -> {
            user.setAttempt(0);
            repositoryUser.save(user);
        });
    }
    public void validateTOTP(String userName, Integer secretKey) {
        log.info("Validating TOTP for user: {}", userName);
        User user = repositoryUser.findByUsername(userName).orElseThrow(() -> new BadCredentialsException("User not found"));
        String secret = user.getSecret();
        try {
            secret = e2EE.decrypt(secret);
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
                e2EE.decrypt(secret),
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
        User user = repositoryUser.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        // User userCurrent = repositoryUser.findByUsername(currentUser).orElseThrow(() -> new EntityNotFoundException("Current user not found"));
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
        User user = repositoryUser.findByUsername(username.trim())
                .orElseThrow(() -> new EntityNotFoundException("Resource not found"));
        // String currentUsername = Information.getCurrentUser().orElse(null);
        // if (currentUsername == null) {
        //     return user;
        // }
        // User currentUser = repositoryUser.findByUsername(currentUsername)
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
