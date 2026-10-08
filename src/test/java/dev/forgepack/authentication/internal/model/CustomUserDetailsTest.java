package dev.forgepack.authentication.internal.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void getAuthorities_returnsEmptySet() {
        CustomUserDetails details = new CustomUserDetails();
        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void isAccountNonLocked_reflectsAttempts() {
        CustomUserDetails details = new CustomUserDetails();
        assertThat(details.isAccountNonLocked()).isTrue();
        details.setAttempt(4);
        assertThat(details.isAccountNonLocked()).isTrue();
        details.setAttempt(5);
        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    void isEnabled_reflectsActiveFlag() {
        CustomUserDetails details = new CustomUserDetails();
        assertThat(details.isEnabled()).isFalse();
        details.setActive(true);
        assertThat(details.isEnabled()).isTrue();
    }
}