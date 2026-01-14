package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.dto.PeriodRequest;
import com.example.pcos.health.tracker.dto.CycleRequest;
import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.PeriodCycle;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.PeriodCycleRepository;
import com.example.pcos.health.tracker.security.AuthContext;
import com.example.pcos.health.tracker.service.CycleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cycles")
public class CycleController {

    @Autowired
    private CycleService cycleService;

    @Autowired
    private PeriodCycleRepository periodCycleRepository;

    @Autowired
    private AuthContext authContext;

    // =================================================
    // ⭐ SAVE PERIOD (Bleeding Days)
    // =================================================
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

    // =================================================
    // ⭐ CREATE A CYCLE (Calendar Range Save)
    // =================================================
    @PostMapping
    public ResponseEntity<Cycle> createCycle(
            @RequestBody CycleRequest request
    ) {
        User currentUser = authContext.getCurrentUser();

        Cycle savedCycle = cycleService.createCycle(
                request.getStartDate(),
                request.getEndDate(),
                currentUser
        );

        return ResponseEntity.ok(savedCycle);
    }

    // =================================================
    // ⭐ GET LOGGED-IN USER CYCLES
    // =================================================
    @GetMapping("/my-cycles")
    public ResponseEntity<List<Cycle>> getMyCycles() {

        User currentUser = authContext.getCurrentUser();

        List<Cycle> cycles =
                cycleService.getUserCycles(currentUser.getId());

        return ResponseEntity.ok(cycles);
    }

    // =================================================
    // ⭐ GET USER PERIOD HISTORY (Calendar Highlight)
    // =================================================
    @GetMapping("/my-periods")
    public ResponseEntity<List<PeriodCycle>> getMyPeriods() {

        User currentUser = authContext.getCurrentUser();

        List<PeriodCycle> periods =
                periodCycleRepository.findByUserIdOrderByStartDateDesc(
                        currentUser.getId()
                );

        return ResponseEntity.ok(periods);
    }

    // =================================================
    // ⭐ GET CYCLE BY ID (Ownership Protected)
    // =================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getCycleById(@PathVariable Long id) {

        User currentUser = authContext.getCurrentUser();

        return cycleService.getCycleById(id, currentUser);
    }

    // =================================================
    // ⭐ DELETE CYCLE (Ownership Protected)
    // =================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCycle(@PathVariable Long id) {

        User currentUser = authContext.getCurrentUser();

        boolean deleted = cycleService.deleteCycle(id, currentUser);

        if (!deleted) {
            return ResponseEntity.status(403)
                    .body("You cannot delete another user's data");
        }

        return ResponseEntity.ok("Cycle deleted successfully");
    }
}
