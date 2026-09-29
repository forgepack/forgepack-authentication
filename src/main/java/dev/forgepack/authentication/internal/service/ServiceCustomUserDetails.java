package dev.forgepack.authentication.internal.service;

import dev.forgepack.authorization.internal.repository.RepositoryUser;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import dev.forgepack.authentication.internal.model.CustomUserDetails;

@Service
public class ServiceCustomUserDetails implements UserDetailsService {

    private final RepositoryUser repositoryUser;

    public ServiceCustomUserDetails(RepositoryUser repositoryUser) {
        this.repositoryUser = repositoryUser;
    }
    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) {
        return (CustomUserDetails) repositoryUser.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Resource not found"));
    }
}
