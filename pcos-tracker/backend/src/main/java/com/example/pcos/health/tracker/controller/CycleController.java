package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.dto.PeriodRequest;
import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.PeriodCycle;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.CycleRepository;
import com.example.pcos.health.tracker.repository.PeriodCycleRepository;
import com.example.pcos.health.tracker.security.AuthContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/cycles")
public class CycleController {

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private PeriodCycleRepository periodCycleRepository;

    @Autowired
    private AuthContext authContext;

    // ==============================
    // ⭐ SAVE PERIOD (Start + End)
    // ==============================
    @PostMapping("/period")
    public ResponseEntity<?> savePeriod(@RequestBody PeriodRequest request) {

        User currentUser = authContext.getCurrentUser();

        PeriodCycle period = new PeriodCycle();
        period.setUser(currentUser);
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());

        periodCycleRepository.save(period);

        return ResponseEntity.ok("Period saved successfully");
    }

    // ==============================
    // ⭐ CREATE A CYCLE
    // ==============================
    @PostMapping
    public ResponseEntity<?> createCycle(@RequestBody Cycle cycle) {

        System.out.println(" CREATE CYCLE API HIT");
        System.out.println("Incoming payload: " + cycle);

        User currentUser = authContext.getCurrentUser();
        System.out.println("Current user: " + currentUser.getEmail());

        cycle.setUser(currentUser);

        long days = ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate());
        cycle.setDuration((int) days);

        Cycle saved = cycleRepository.save(cycle);
        System.out.println("✅ SAVED CYCLE ID: " + saved.getId());

        return ResponseEntity.ok(saved);
    }

    // ==============================
    // ⭐ GET USER CYCLES
    // ==============================
    @GetMapping("/my-cycles")
    public ResponseEntity<List<Cycle>> getMyCycles() {

        User currentUser = authContext.getCurrentUser();

        List<Cycle> cycles =
                cycleRepository.findByUserIdOrderByStartDateDesc(
                        currentUser.getId()
                );

        return ResponseEntity.ok(cycles);
    }

    // ==============================
    // ⭐ GET CYCLE BY ID
    // ==============================
    @GetMapping("/{id}")
    public ResponseEntity<?> getCycleById(@PathVariable Long id) {

        User currentUser = authContext.getCurrentUser();

        return cycleRepository.findById(id)
                .map(cycle -> {
                    if (!cycle.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("Access denied");
                    }
                    return ResponseEntity.ok(cycle);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==============================
    // ⭐ DELETE CYCLE
    // ==============================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCycle(@PathVariable Long id) {

        User currentUser = authContext.getCurrentUser();

        return cycleRepository.findById(id)
                .map(cycle -> {
                    if (!cycle.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403)
                                .body("You cannot delete another user's data");
                    }

                    cycleRepository.delete(cycle);
                    return ResponseEntity.ok("Cycle deleted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
