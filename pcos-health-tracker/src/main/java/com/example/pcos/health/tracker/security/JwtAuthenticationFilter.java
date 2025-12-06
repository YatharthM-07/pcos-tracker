package com.example.pcos.health.tracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("----- FILTER CALLED for " + path + " -----");

        // 🔥 Skip JWT for authentication endpoints
        if (path.startsWith("/auth")) {
            System.out.println("Skipping JWT for public auth route.");
            filterChain.doFilter(request, response);
            return;
        }

        // Read Authorization header
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header = " + authHeader);

        String token = null;
        String email = null;

        // Extract Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("Extracted Token = " + token);

            try {
                email = jwtUtil.extractEmail(token);
                System.out.println("Email extracted = " + email);
            } catch (Exception e) {
                System.out.println("❌ Token extraction failed: " + e.getMessage());
            }
        }

        // If token email extracted + no existing authentication → authenticate the user
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Loading user details for: " + email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("✔ JWT validated. User authenticated: " + email);
            } else {
                System.out.println("❌ JWT validation FAILED.");
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
