package dev.forgepack.authentication.internal.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    // Current implementation evaluates against a freshly created instance rather than `this`,
    // so these accessors always reflect the default (empty/null) state regardless of the field values set below.
    @Test
    void getAuthorities_returnsEmptySet() {
        CustomUserDetails details = new CustomUserDetails();
        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void isAccountNonLocked_isAlwaysTrue() {
        CustomUserDetails details = new CustomUserDetails();
        details.setAttempt(10);
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void isEnabled_isAlwaysFalse() {
        CustomUserDetails details = new CustomUserDetails();
        details.setActive(true);
        assertThat(details.isEnabled()).isFalse();
    }
}
