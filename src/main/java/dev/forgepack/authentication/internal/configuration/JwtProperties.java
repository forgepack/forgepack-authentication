package dev.forgepack.authentication.internal.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for JWT generation and validation.
 *
 * <p>Configurable via {@code application.properties} under the {@code forgepack.jwt}
 * prefix. All fields have sensible defaults and may be omitted by the consumer.</p>
 *
 * <p><strong>Warning:</strong> when {@code secret} is left blank, a random in-memory
 * key is generated at startup. All tokens are invalidated on every restart.
 * When {@code secret} is non-blank but shorter than 64 bytes, a warning is logged
 * at startup indicating a weak signing key.</p>
 *
 * @param issuer     Token issuer claim (must not be blank). Default: {@code forgepack}.
 * @param audience   Token audience claim (must not be blank). Default: {@code forgepack-client}.
 * @param expiration Token lifetime in milliseconds (must be &gt;= 1). Default: {@code 3600000} (1 hour).
 * @param secret     HMAC-SHA512 signing secret. Minimum 64 bytes recommended. Default: blank (random key).
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 */
@Validated
@ConfigurationProperties(prefix = "forgepack.jwt")
public record JwtProperties(
        @NotBlank @DefaultValue("forgepack")        String issuer,
        @NotBlank @DefaultValue("forgepack-client") String audience,
        @Min(1)   @DefaultValue("3600000")          long   expiration,
                  @DefaultValue("")                 String secret
) {
    @Override
    public String toString() {
        return "JwtProperties[issuer='" + issuer + "', audience='" + audience +
               "', expiration=" + expiration + ", secret='" + (secret == null || secret.isBlank() ? "<random>" : "***") + "']";
    }
}
