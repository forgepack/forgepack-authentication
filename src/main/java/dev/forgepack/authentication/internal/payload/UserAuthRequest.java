package dev.forgepack.authentication.internal.payload;

import dev.forgepack.validation.api.annotation.HasDigit;
import dev.forgepack.validation.api.annotation.HasLetter;
import dev.forgepack.validation.api.annotation.HasUpperCase;
import dev.forgepack.validation.api.annotation.HasLowerCase;
import dev.forgepack.validation.api.annotation.HasLength;
import dev.forgepack.core.api.payload.DTOIdentifiable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) for user authentication requests.
 *
 * <p>This record encapsulates the input data required for authentication-related
 * operations, such as login or token generation. It includes validation constraints
 * to ensure credential integrity before processing.</p>
 *
 * <h3>Validation Rules</h3>
 * <ul>
 *     <li><b>username</b>: required, must not be blank</li>
 *     <li><b>password</b>: required, must not be blank and must satisfy complexity rules:
 *         <ul>
 *             <li>Contains at least one digit</li>
 *             <li>Contains at least one letter</li>
 *             <li>Contains at least one uppercase character</li>
 *             <li>Contains at least one lowercase character</li>
 *             <li>Meets minimum length requirements</li>
 *         </ul>
 *     </li>
 *     <li><b>secret</b>: required, used for multi-factor authentication (2FA)</li>
 * </ul>
 *
 * <h3>Usage Context</h3>
 * <ul>
 *     <li>Used as input for authentication endpoints (e.g., login)</li>
 *     <li>Supports integration with multi-factor authentication mechanisms</li>
 *     <li>Can be extended to include additional security layers (e.g., CAPTCHA)</li>
 * </ul>
 *
 * <h3>Security Considerations</h3>
 * <ul>
 *     <li>Credentials should be transmitted over secure channels (HTTPS)</li>
 *     <li>Password must never be logged or exposed in plain text</li>
 *     <li>The <b>secret</b> field should be handled securely and validated against a trusted source</li>
 * </ul>
 *
 * <h3>Architectural Notes</h3>
 * <ul>
 *     <li>This DTO does not contain persistence logic</li>
 *     <li>Authentication logic must be handled in the service layer</li>
 *     <li>Validation annotations are processed via Bean Validation</li>
 * </ul>
 *
 * @param id optional identifier (generally not required for authentication)
 * @param username user identifier used for authentication
 * @param password user credential with enforced complexity rules
 * @param secret one-time or secondary authentication factor (2FA)
 *
 * @author Marcelo Ribeiro Gadelha
 * @version 1.0
 * @since 1.0
 *
 * @see DTOIdentifiable
 */
public record UserAuthRequest(

        UUID id,
        @NotNull(message = "{not.null}") @NotBlank(message = "{not.blank}")
        String username,
        @NotNull(message = "{not.null}") @NotBlank(message = "{not.blank}")
        @HasDigit
        @HasLetter
        @HasUpperCase
        @HasLowerCase
        @HasLength
        String password,
        @NotNull(message = "{not.null}")
        Integer secret
//    String captchaToken
) implements DTOIdentifiable<UUID> {}
