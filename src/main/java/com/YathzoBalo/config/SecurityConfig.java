package com.YathzoBalo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API development
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/test/**").permitAll() // Allow test endpoints
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                        .requestMatchers("/api/public/**").permitAll() // For future public endpoints
                        .anyRequest().authenticated() // Everything else needs auth
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable()) // Modern way to disable frame options
                );

        return http.build();
    }
}