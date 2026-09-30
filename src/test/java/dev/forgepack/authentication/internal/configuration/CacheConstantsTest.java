package dev.forgepack.authentication.internal.configuration;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConstantsTest {

    @Test
    void constants_haveExpectedValues() {
        assertThat(CacheConstants.ROLES_CACHE).isEqualTo("user-roles");
        assertThat(CacheConstants.PERMISSIONS_CACHE).isEqualTo("user-permissions");
    }

    @Test
    void privateConstructor_isNotUsedButAccessible() throws Exception {
        Constructor<CacheConstants> constructor = CacheConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThat(constructor.newInstance()).isNotNull();
    }
}
