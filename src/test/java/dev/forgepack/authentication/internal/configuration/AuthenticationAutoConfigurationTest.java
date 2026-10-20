package dev.forgepack.authentication.internal.configuration;

import dev.forgepack.authentication.internal.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Configuration(proxyBeanMethods = false)
    static class CustomPasswordEncoderConfiguration {

        @Bean
        PasswordEncoder customPasswordEncoder() {
            return new BCryptPasswordEncoder(4);
        }
    }
}
