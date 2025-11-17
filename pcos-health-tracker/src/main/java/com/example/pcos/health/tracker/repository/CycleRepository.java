package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CycleRepository extends JpaRepository<Cycle, Long> {

    // Fetch cycles of a specific user, newest first
    List<Cycle> findByUserIdOrderByStartDateDesc(Long userId);
}
