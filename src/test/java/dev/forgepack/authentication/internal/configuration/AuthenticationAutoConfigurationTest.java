package dev.forgepack.authentication.internal.configuration;

import dev.forgepack.authentication.internal.mapper.TokenMapper;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
import dev.forgepack.core.api.mapper.Mapper;
import dev.forgepack.authentication.internal.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthenticationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuthenticationAutoConfiguration.class))
            .withBean(
                    CustomUserDetailsService.class,
                    () -> mock(CustomUserDetailsService.class));

    @Test
    void providesBCryptPasswordEncoderByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PasswordEncoder.class);
            assertThat(context.getBean(PasswordEncoder.class)).isInstanceOf(BCryptPasswordEncoder.class);
        });
    }

    @Test
    void preservesConsumerPasswordEncoder() {
        contextRunner.withUserConfiguration(CustomPasswordEncoderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordEncoder.class);
                    assertThat(context.getBean(PasswordEncoder.class))
                            .isSameAs(context.getBean("customPasswordEncoder"));
                });
    }

    @Test
    void providesTokenMapper() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TokenMapper.class);
            assertThat(context).hasSingleBean(Mapper.class);
            assertThat(context.getBean(Mapper.class))
                    .isInstanceOf(TokenMapper.class);
        });
    }

    @Test
    void isPublishedAsAutoConfiguration() throws IOException {
        try (var inputStream = AuthenticationAutoConfiguration.class.getClassLoader()
                .getResourceAsStream("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")) {
            assertThat(inputStream).isNotNull();
            assertThat(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                    .contains(AuthenticationAutoConfiguration.class.getName());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomPasswordEncoderConfiguration {

        @Bean
        PasswordEncoder customPasswordEncoder() {
            return new BCryptPasswordEncoder(4);
        }
    }
}
