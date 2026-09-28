package dev.forgepack.authentication.internal.configuration.filter;

import dev.forgepack.authentication.internal.configuration.JwtConfiguration;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT authentication filter that intercepts every HTTP request to validate
 * and establish the security context from a Bearer token.
 *
 * <p>Extends {@link OncePerRequestFilter} to guarantee single execution per request.
 * If a valid JWT is present in the {@code Authorization} header, the associated
 * user is loaded and set in the {@link SecurityContextHolder}.</p>
 *
 * <p>Authentication errors are logged and silently swallowed so the filter
 * chain always continues.</p>
 *
 * @author Marcelo Ribeiro Gadelha
 * @since 1.0
 *
 * @see JwtConfiguration
 * @see UserDetailsService
 */
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtConfiguration   jwtConfiguration;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtConfiguration jwtConfiguration, UserDetailsService userDetailsService) {
        this.jwtConfiguration  = jwtConfiguration;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Processes each HTTP request to extract and validate a JWT from the
     * {@code Authorization: Bearer <token>} header.
     *
     * <p>When a valid token is found, the corresponding user is loaded via
    * {@link UserDetailsService} and stored in the {@link SecurityContextHolder}.
     * Authentication errors are logged as warnings and silently swallowed so the
     * filter chain always continues.</p>
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs while processing the request
     * @throws IOException      if an I/O error occurs during filter execution
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            getJwtFromRequest(request)
                    .filter(jwtConfiguration::validateJwt)
                    .map(jwtConfiguration::getUsernameFromJwt)
                    .ifPresent(username -> authenticateUser(username, request));
        } catch (JwtException ex) {
            log.warn("Unable to authenticate user: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return Optional.of(token.substring(7));
        }
        return Optional.empty();
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) return;
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
