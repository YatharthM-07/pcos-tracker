package com.example.pcos.health.tracker.security;

import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email);
    }
}
