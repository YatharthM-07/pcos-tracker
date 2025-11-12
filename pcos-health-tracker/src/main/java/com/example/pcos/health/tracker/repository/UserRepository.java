package com.example.pcos.health.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.pcos.health.tracker.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}