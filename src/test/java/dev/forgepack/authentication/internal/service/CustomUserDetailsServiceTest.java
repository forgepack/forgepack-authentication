package dev.forgepack.authentication.internal.service;

import dev.forgepack.authentication.internal.model.CustomUserDetails;
import dev.forgepack.authorization.internal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(customUserDetails));

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
        UserDetails result = service.loadUserByUsername("john");

        assertThat(result).isSameAs(customUserDetails);
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
