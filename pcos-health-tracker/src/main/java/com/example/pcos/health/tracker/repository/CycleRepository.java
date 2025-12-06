package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CycleRepository extends JpaRepository<Cycle, Long> {

    // Fetch cycles by User object
    List<Cycle> findByUser(User user);

    // Fetch cycles by userId, newest first
    List<Cycle> findByUserIdOrderByStartDateDesc(Long userId);
}
