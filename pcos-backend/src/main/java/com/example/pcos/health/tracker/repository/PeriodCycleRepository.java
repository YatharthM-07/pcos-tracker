package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.PeriodCycle;
import com.example.pcos.health.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeriodCycleRepository extends JpaRepository<PeriodCycle, Long> {
    List<PeriodCycle> findByUserOrderByStartDateDesc(User user);

    List<PeriodCycle> findByUserIdOrderByStartDateDesc(Long id);
}
