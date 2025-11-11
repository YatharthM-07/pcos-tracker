package com.example.pcos.health.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.pcos.health.tracker.entity.Cycle;

public interface CycleRepository extends JpaRepository<Cycle, Long> {
}
