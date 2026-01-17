package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.dto.AuthResponse;
import com.example.pcos.health.tracker.dto.LoginRequest;
import com.example.pcos.health.tracker.dto.SignupRequest;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.UserRepository;
import com.example.pcos.health.tracker.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    // =========================
    // SIGNUP
    // =========================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()) != null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        System.out.println("LOGIN ATTEMPT EMAIL = " + request.getEmail());

        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            System.out.println("LOGIN FAILED → USER NOT FOUND");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }

        System.out.println("DB PASSWORD = " + user.getPassword());
        System.out.println("RAW PASSWORD = " + request.getPassword());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("LOGIN FAILED → PASSWORD MISMATCH");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        System.out.println("LOGIN SUCCESS");

        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    }


    // =========================
    // CURRENT USER
    // =========================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        return ResponseEntity.ok(new Object() {
            public final Long id = user.getId();
            public final String name = user.getName();
            public final String email = user.getEmail();
        });
    }
}
