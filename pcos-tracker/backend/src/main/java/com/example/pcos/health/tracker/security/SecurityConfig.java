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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // ✅ PUBLIC HTML PAGES
                        .requestMatchers(
                                "/",
                                "/index",
                                "/login",
                                "/register",
                                "/user-dashboard",
                                "/nutrition"
                        ).permitAll()

                        // ✅ STATIC RESOURCES
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // ✅ AUTH APIs
                        .requestMatchers("/auth/**").permitAll()

                        // 🔒 PROTECTED APIs (JWT REQUIRED)
                                .requestMatchers(
                                        "/",
                                        "/index",
                                        "/login",
                                        "/register",
                                        "/user-dashboard",
                                        "/cycle-tracker",   // ✅ ADD THIS
                                        "/nutrition"
                                ).permitAll()


// ✅ FOOD APIs (TEMP PUBLIC FOR UI)
                                .requestMatchers("/food/**").permitAll()
                        // 🔒 EVERYTHING ELSE
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}
