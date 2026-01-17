package com.example.pcos.health.tracker.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    // ------------------------------------------------
    // PASSWORD ENCODER
    // ------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ------------------------------------------------
    // AUTH MANAGER
    // ------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ------------------------------------------------
    // SECURITY FILTER CHAIN
    // ------------------------------------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Disable CSRF (JWT-based app)
                .csrf(csrf -> csrf.disable())

                // 🔒 Stateless session (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 🔐 Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // 🌐 PUBLIC HTML PAGES (Thymeleaf)
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/register",
                                "/user-dashboard",
                                "/cycle-tracker",
                                "/symptom-tracker",
                                "/nutrition",
                                "/reports-page"
                        ).permitAll()

                        // 🌐 STATIC RESOURCES
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/static/**"
                        ).permitAll()

                        // 🔓 AUTH ENDPOINTS
                        .requestMatchers("/auth/**").permitAll()

                        // 🟡 FOOD (temporary public)
                        .requestMatchers("/food/**").permitAll()

                        // 🔒 PROTECTED APIs (JWT REQUIRED)
                        .requestMatchers(
                                "/analytics/**",
                                "/daily-log/**",
                                "/cycle/**",
                                "/reports/**",
                                "/symptoms/**"
                        ).authenticated()

                        // 🔒 EVERYTHING ELSE
                        .anyRequest().authenticated()
                );

        // 🔑 JWT filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
