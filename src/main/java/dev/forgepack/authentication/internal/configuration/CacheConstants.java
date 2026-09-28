package dev.forgepack.authentication.internal.configuration;

/**
 * Defines the cache names used by the application.
 *
 * <p>Centralizes cache identifiers to avoid hard-coded string literals and
 * ensure consistency across the caching infrastructure.</p>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 */
public final class CacheConstants {

    private CacheConstants() {}
    public static final String ROLES_CACHE = "user-roles";
    public static final String PERMISSIONS_CACHE = "user-permissions";
}