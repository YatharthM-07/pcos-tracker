package com.example.pcos.health.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.pcos.health.tracker.User;

public interface UserRepository extends JpaRepository<User, Long> {
}