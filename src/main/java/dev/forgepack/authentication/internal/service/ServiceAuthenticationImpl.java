package dev.forgepack.authentication.internal.service;

import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.authentication.api.service.ServiceAuthentication;
import dev.forgepack.security.internal.configuration.ConfigurationJwt;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authorization.internal.model.User;
import dev.forgepack.authorization.internal.repository.RepositoryUser;
import dev.forgepack.authentication.internal.payload.DTORequestToken;
import dev.forgepack.authentication.internal.payload.DTORequestUserAuth;
import dev.forgepack.authentication.internal.payload.DTOResponseToken;
import dev.forgepack.authentication.internal.repository.RepositoryToken;
import dev.forgepack.utils.internal.utils.E2EE;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceAuthenticationImpl implements ServiceAuthentication {

    //    private final ServiceRecaptcha serviceRecaptcha;
    private final E2EE e2EE;
    private final AuthenticationManager authenticationManager;
    private final ConfigurationJwt configurationJwt;
    private final RepositoryToken repositoryToken;
    private final RepositoryUser repositoryUser;
    private final Mapper<Token, DTORequestToken, DTOResponseToken> mapper;
    private final ServiceCustomUserDetails serviceCustomUserDetails;
    private static final Logger log = LoggerFactory.getLogger(ServiceAuthenticationImpl.class);

    public ServiceAuthenticationImpl(E2EE e2EE, AuthenticationManager authenticationManager, ConfigurationJwt configurationJwt, RepositoryToken repositoryToken, RepositoryUser repositoryUser, Mapper<Token, DTORequestToken, DTOResponseToken> mapper, ServiceCustomUserDetails serviceCustomUserDetails) {
        this.e2EE = e2EE;
        this.authenticationManager = authenticationManager;
        this.configurationJwt = configurationJwt;
        this.repositoryToken = repositoryToken;
        this.repositoryUser = repositoryUser;
        this.mapper = mapper;
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
            String token = configurationJwt.generateToken(authentication.getName());
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
    @Override
    public DTOResponseToken refresh(DTORequestToken dtoRequestToken) {
        if (repositoryToken.existsByRefreshToken(dtoRequestToken.refreshToken()) &&
                configurationJwt.validateJwt(dtoRequestToken.accessToken())) {
            UserDetails userDetails = serviceCustomUserDetails.loadUserByUsername(
                    configurationJwt.getUsernameFromJwt(dtoRequestToken.accessToken())
            );
            String tokenResponse = configurationJwt.generateToken(configurationJwt.getUsernameFromJwt(dtoRequestToken.accessToken()));
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
                    return mapper.toResponse(token);
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
}
