package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userRepository.save(user);
    }


    @GetMapping
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}
