package dev.forgepack.authentication.internal.configuration;

import dev.forgepack.authentication.internal.mapper.TokenMapper;
import dev.forgepack.authentication.internal.model.Token;
import dev.forgepack.authentication.internal.payload.TokenRequest;
import dev.forgepack.authentication.internal.payload.TokenResponse;
import dev.forgepack.authentication.internal.service.CustomUserDetailsService;
import dev.forgepack.core.api.mapper.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@AutoConfiguration
public class AuthenticationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TokenMapper.class)
    Mapper<Token, TokenRequest, TokenResponse> tokenMapper() {
        return new TokenMapper();
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider) {

        return new ProviderManager(authenticationProvider);
    }
}
