package dev.forgepack.authentication.internal.configuration;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Component responsible for JWT generation, parsing, and validation.
 *
 * <p>Tokens are signed with {@code HmacSHA512} using the secret configured via
 * {@code forgepack.jwt.secret}. If no secret is configured, a random in-memory
 * key is generated at startup and a warning is logged — all tokens are invalidated
 * on restart.</p>
 *
 * <p>Configurable properties under {@code forgepack.jwt}:</p>
 * <ul>
 *     <li>{@code issuer}     – token issuer claim</li>
 *     <li>{@code audience}   – token audience claim</li>
 *     <li>{@code expiration} – token lifetime in milliseconds</li>
 *     <li>{@code secret}     – HMAC signing secret (min. 64 bytes recommended)</li>
 * </ul>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 */
@Component
public class JwtConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtConfiguration.class);

    private final JwtProperties props;
    private final SecretKey     signingKey;

    public JwtConfiguration(JwtProperties props) {
        this.props      = props;
        this.signingKey = buildSigningKey(props.secret());
    }

    /**
     * Generates a signed JWT for the given subject (username).
     *
     * @param subject the subject to embed in the token (typically the username)
     * @return the compact serialized JWT string
     */
    public String generateToken(String subject) {
        return Jwts.builder()
                .audience().add(props.audience()).and()
                .header().add("typ", "JWT").and()
                .issuer(props.issuer())
                .subject(subject)
                .notBefore(new Date())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + props.expiration()))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (username) from a signed JWT.
     *
     * @param token the compact serialized JWT string
     * @return the username stored in the token's subject claim
     */
    public String getUsernameFromJwt(String token) {
        return Jwts.parser()
                .verifyWith(signingKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates a JWT by verifying its signature and claims.
     *
     * <p>Returns {@code false} and logs the cause for any of the following conditions:
     * invalid signature, malformed token, expired token, unsupported token, or
     * empty claims string.</p>
     *
     * @param token the compact serialized JWT string
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateJwt(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature or token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Builds the {@link SecretKey} used to sign and verify JWT tokens.
     *
     * <p>Falls back to a random in-memory key when {@code forgepack.jwt.secret}
     * is blank, logging a warning to alert about the insecure state.</p>
     *
     * @param secret the configured secret string
     * @return the {@link SecretKey} for HMAC-SHA512 signing
     */
    private static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            log.warn("JWT secret not configured (forgepack.jwt.secret). Using random in-memory key — all tokens will be invalidated on restart.");
            byte[] randomKey = new byte[64];
            new SecureRandom().nextBytes(randomKey);
            return new SecretKeySpec(randomKey, "HmacSHA512");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 64) {
            log.warn("JWT secret is shorter than the recommended 64 bytes (current: {} bytes). Consider using a stronger secret.",
                    secret.getBytes(StandardCharsets.UTF_8).length);
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }
}
