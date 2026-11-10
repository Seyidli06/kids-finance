package com.kidsfinance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        // =========================================
                        // Public endpoints
                        // =========================================

                        .requestMatchers(
                                "/actuator/health",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/parents",
                                "/api/auth/login"
                        ).permitAll()


                        // =========================================
                        // Parent - children
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parents/me/children"
                        ).hasRole("PARENT")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/parents/me/children"
                        ).hasRole("PARENT")


                        // =========================================
                        // Parent - wallet
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parents/me/children/*/wallet"
                        ).hasRole("PARENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parents/me/children/*/wallet/transactions"
                        ).hasRole("PARENT")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/parents/me/children/*/wallet/credit"
                        ).hasRole("PARENT")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/parents/me/children/*/wallet/debit"
                        ).hasRole("PARENT")


                        // =========================================
                        // Parent - progress
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parents/me/children/*/progress"
                        ).hasRole("PARENT")


                        // =========================================
                        // Parent - main simulation history
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parents/me/children/*/simulations/runs"
                        ).hasRole("PARENT")


                        // =========================================
                        // Child - profile / wallet / progress
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/children/me",
                                "/api/children/me/wallet",
                                "/api/children/me/wallet/transactions",
                                "/api/children/me/progress"
                        ).hasRole("CHILD")


                        // =========================================
                        // Child - challenges / practice
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/children/me/challenges",
                                "/api/children/me/challenges/attempts"
                        ).hasRole("CHILD")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/children/me/challenges/*/answers"
                        ).hasRole("CHILD")


                        // =========================================
                        // Child - main simulation
                        // =========================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/children/me/simulations/runs"
                        ).hasRole("CHILD")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/children/me/simulations/runs"
                        ).hasRole("CHILD")


                        // =========================================
                        // Everything else blocked
                        // =========================================

                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName(
                "role"
        );

        authoritiesConverter.setAuthorityPrefix(
                "ROLE_"
        );

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }
}