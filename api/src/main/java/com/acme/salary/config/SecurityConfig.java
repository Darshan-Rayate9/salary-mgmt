package com.acme.salary.config;

import com.acme.salary.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Single-user HTTP Basic auth. The persona is one org-wide HR Manager (see
 * REQUIREMENTS.md - RBAC is explicitly out of scope), so there is no user
 * table and no token lifecycle to manage: one in-memory account, credentials
 * supplied per request. Spring Security's BasicAuthenticationFilter checks
 * each request against the {@link InMemoryUserDetailsManager} below.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The one HR Manager account, seeded from config (override the password via
     * SEED_ADMIN_PASSWORD in any real deployment - the default is local-dev only).
     */
    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin-username}") String adminUsername,
            @Value("${app.seed.admin-password}") String adminPassword) {
        UserDetails hrManager = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("HR_MANAGER")
                .build();
        return new InMemoryUserDetailsManager(hrManager);
    }

    /**
     * Returns 401 as JSON in our error-contract shape and, crucially, without a
     * {@code WWW-Authenticate: Basic} header - that header would trigger the
     * browser's native basic-auth popup, which we don't want in front of an SPA
     * that manages the credentials itself.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(
                    response.getWriter(),
                    ErrorResponse.of("AUTH_REQUIRED", "Authentication is required to access this resource"));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // stateless API, no cookie-based session to protect
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Angular's static build, served from the same origin (see ARCHITECTURE.md).
                        .requestMatchers("/", "/index.html", "/assets/**", "/*.js", "/*.css", "/*.ico").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint));
        return http.build();
    }
}
