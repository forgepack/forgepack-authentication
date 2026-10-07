package dev.forgepack.authentication.internal.configuration;

import com.github.benmanes.caffeine.cache.LoadingCache;
import dev.forgepack.authorization.internal.model.Privilege;
import dev.forgepack.authorization.internal.model.Role;
import dev.forgepack.authorization.internal.model.User;
import dev.forgepack.authorization.internal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheAutoConfigurationTest {

    @Mock
    private UserRepository userRepository;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        CacheProperties cacheProperties = new CacheProperties(Duration.ofMinutes(10), Duration.ofMinutes(5), 10, 100);
        CacheAutoConfiguration configuration = new CacheAutoConfiguration(userRepository, cacheProperties);
        SimpleCacheManager manager = (SimpleCacheManager) configuration.cacheManager();
        // SimpleCacheManager only populates its lookup map once Spring invokes afterPropertiesSet(); trigger it manually here.
        manager.afterPropertiesSet();
        cacheManager = manager;
    }

    @SuppressWarnings("unchecked")
    private LoadingCache<Object, Object> nativeCache(String cacheName) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(cacheName);
        assertThat(caffeineCache).isNotNull();
        return (LoadingCache<Object, Object>) caffeineCache.getNativeCache();
    }

    @Test
    void rolesCache_loadsRoleNamesForExistingUser() {
        Privilege privilege = new Privilege("READ_USER");
        Role role = new Role("ADMIN", Set.of(privilege));
        User user = new User("john", "john@forgepack.dev", new HashSet<>(Set.of(role)));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Object result = nativeCache(CacheConstants.ROLES_CACHE).get("john");

        assertThat(result).isEqualTo(Set.of("ADMIN"));
    }

    @Test
    void rolesCache_returnsEmptySetForMissingUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        Object result = nativeCache(CacheConstants.ROLES_CACHE).get("ghost");

        assertThat((Set<?>) result).isEmpty();
    }

    @Test
    void permissionsCache_loadsPrivilegeNamesForExistingUser() {
        Privilege privilege = new Privilege("READ_USER");
        Role role = new Role("ADMIN", Set.of(privilege));
        User user = new User("john", "john@forgepack.dev", new HashSet<>(Set.of(role)));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Object result = nativeCache(CacheConstants.PERMISSIONS_CACHE).get("john");

        assertThat(result).isEqualTo(Set.of("READ_USER"));
    }

    @Test
    void permissionsCache_returnsEmptySetForMissingUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        Object result = nativeCache(CacheConstants.PERMISSIONS_CACHE).get("ghost");

        assertThat((Set<?>) result).isEmpty();
    }
}
