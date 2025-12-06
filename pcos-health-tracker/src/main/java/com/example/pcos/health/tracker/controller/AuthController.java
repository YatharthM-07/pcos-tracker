package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.dto.LoginRequest;
import com.example.pcos.health.tracker.dto.SignupRequest;
import com.example.pcos.health.tracker.dto.AuthResponse;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.UserRepository;
import com.example.pcos.health.tracker.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ⭐ SIGNUP API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.status(400).body("Email already exists.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // hash password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    // ⭐ LOGIN API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);  // remove "Bearer "
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.ok(
                new Object() {
                    public final Long id = user.getId();
                    public final String name = user.getName();
                    public final String emailAddress = user.getEmail();
                }
        );
    }

}
