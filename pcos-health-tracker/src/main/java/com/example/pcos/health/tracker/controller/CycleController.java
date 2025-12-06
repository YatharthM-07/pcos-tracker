package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.CycleRepository;
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
    private AuthContext authContext;

    // ⭐ CREATE A NEW CYCLE (belongs to logged-in user)
    @PostMapping
    public ResponseEntity<?> createCycle(@RequestBody Cycle cycle) {

        User currentUser = authContext.getCurrentUser();
        cycle.setUser(currentUser);  // attach logged-in user

        // ⭐ Auto-calculate duration
        long days = ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate());
        cycle.setDuration((int) days);

        Cycle saved = cycleRepository.save(cycle);

        return ResponseEntity.ok(saved);
    }

    // ⭐ GET ALL CYCLES OF LOGGED-IN USER
    @GetMapping("/my-cycles")
    public ResponseEntity<List<Cycle>> getMyCycles() {

        User currentUser = authContext.getCurrentUser();

        List<Cycle> cycles = cycleRepository
                .findByUserIdOrderByStartDateDesc(currentUser.getId());

        return ResponseEntity.ok(cycles);
    }

    // ⭐ GET A SPECIFIC CYCLE (only if it belongs to the user)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCycleById(@PathVariable Long id) {

        User currentUser = authContext.getCurrentUser();

        return cycleRepository.findById(id)
                .map(cycle -> {
                    if (!cycle.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("Access denied!");
                    }
                    return ResponseEntity.ok(cycle);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ⭐ DELETE A CYCLE (only if it belongs to the user)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCycle(@PathVariable Long id) {

        User currentUser = authContext.getCurrentUser();

        return cycleRepository.findById(id)
                .map(cycle -> {
                    if (!cycle.getUser().getId().equals(currentUser.getId())) {
                        return ResponseEntity.status(403).body("You cannot delete another user's data!");
                    }

                    cycleRepository.delete(cycle);
                    return ResponseEntity.ok("Cycle deleted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
