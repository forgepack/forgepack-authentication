package dev.forgepack.authentication.internal.service;

import dev.forgepack.authorization.internal.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import dev.forgepack.authentication.internal.model.CustomUserDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) {
        return (CustomUserDetails) userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Resource not found"));
    }
}
