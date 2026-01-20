package com.example.pcos.health.tracker.service;

import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.CycleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CycleService {

    @Autowired
    private CycleRepository cycleRepository;

    // =========================================
    // CREATE CYCLE (used by calendar save)
    // =========================================
    public Cycle createCycle(
            LocalDate startDate,
            LocalDate endDate,
            User user
    ) {

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        int duration =
                (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        Cycle cycle = new Cycle();
        cycle.setStartDate(startDate);
        cycle.setEndDate(endDate);
        cycle.setDuration(duration);
        cycle.setUser(user);

        return cycleRepository.save(cycle);
    }

    // =========================================
    // GET ALL CYCLES FOR USER
    // =========================================
    public List<Cycle> getUserCycles(Long userId) {
        return cycleRepository.findByUserIdOrderByStartDateDesc(userId);
    }

    // =========================================
    // GET CYCLE BY ID (ownership check)
    // =========================================
    public ResponseEntity<?> getCycleById(Long id, User currentUser) {

        Optional<Cycle> optionalCycle = cycleRepository.findById(id);

        if (optionalCycle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cycle cycle = optionalCycle.get();

        if (!cycle.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(cycle);
    }

    // =========================================
    // DELETE CYCLE (ownership check)
    // =========================================
    public boolean deleteCycle(Long id, User currentUser) {

        Optional<Cycle> optionalCycle = cycleRepository.findById(id);

        if (optionalCycle.isEmpty()) {
            return false;
        }

        Cycle cycle = optionalCycle.get();

        if (!cycle.getUser().getId().equals(currentUser.getId())) {
            return false;
        }

        cycleRepository.delete(cycle);
        return true;
    }
}
