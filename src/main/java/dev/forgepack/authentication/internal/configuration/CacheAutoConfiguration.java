package dev.forgepack.authentication.internal.configuration;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;

import dev.forgepack.authorization.internal.model.Privilege;
import dev.forgepack.authorization.internal.model.Role;
import dev.forgepack.authorization.internal.repository.UserRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configures the application's caching infrastructure using Spring Cache and Caffeine.
 *
 * <p>Registers dedicated caches for user roles and privileges, leveraging
 * {@code LoadingCache} to automatically load authorization data from the
 * database when it is not available in memory.</p>
 *
 * <p>Cache capacity, maximum size, and expiration policies are defined in
 * {@link CacheProperties}.</p>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 */
@AutoConfiguration(afterName = {
        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
@EnableCaching
@EnableConfigurationProperties({ CacheProperties.class, JwtProperties.class })
@AutoConfigurationPackage(basePackages = "dev.forgepack.authentication")
@ComponentScan(basePackages = "dev.forgepack.authentication",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CacheAutoConfiguration.class))
public class CacheAutoConfiguration {
    private final UserRepository userRepository;
    private final CacheProperties cacheProperties;

    public CacheAutoConfiguration(UserRepository userRepository, CacheProperties cacheProperties) {
        this.userRepository = userRepository;
        this.cacheProperties = cacheProperties;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                buildCache(CacheConstants.ROLES_CACHE, this::loadUserRoles),
                buildCache(CacheConstants.PERMISSIONS_CACHE, this::loadUserPermissions)
        ));
        return cacheManager;
    }
    private CaffeineCache buildCache(String name, CacheLoader<String, Set<String>> loader) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .initialCapacity(cacheProperties.initialCapacity())
                        .maximumSize(cacheProperties.maximumSize())
                        .expireAfterWrite(cacheProperties.ttl())
                        .refreshAfterWrite(cacheProperties.refresh())
                        .recordStats()
                        .build(key -> loader.load(key.toString())));
    }
    private Set<String> loadUserRoles(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
    private Set<String> loadUserPermissions(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRole().stream()
                        .flatMap(role -> role.getPrivilege().stream())
                        .map(Privilege::getName)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
}
