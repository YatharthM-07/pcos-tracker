package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.CycleRepository;
import com.example.pcos.health.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cycles")
public class CycleController {

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private UserRepository userRepository;

    // Simple request payload class (or reuse your DTO if you have)
    public static class CyclePayload {
        public Long userId;
        public String startDate; // "YYYY-MM-DD"
        public String endDate;   // "YYYY-MM-DD"
        public String notes;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCycle(@RequestBody CyclePayload payload) {
        if (payload == null || payload.userId == null || payload.startDate == null || payload.endDate == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId, startDate and endDate required"));
        }

        Optional<User> maybeUser = userRepository.findById(payload.userId);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for id=" + payload.userId));
        }
        User user = maybeUser.get();

        try {
            Cycle cycle = new Cycle();
            cycle.setStartDate(LocalDate.parse(payload.startDate));
            cycle.setEndDate(LocalDate.parse(payload.endDate));
            // calculate duration if your entity expects it (end - start + 1)
            long duration = java.time.temporal.ChronoUnit.DAYS.between(cycle.getStartDate(), cycle.getEndDate()) + 1;
            cycle.setDuration((int) duration);

            cycle.setUser(user);
            // set notes if Cycle has a notes field
            // cycle.setNotes(payload.notes);

            Cycle saved = cycleRepository.save(cycle);
            return ResponseEntity.ok(Map.of("message", "Cycle added", "id", saved.getId(), "duration", saved.getDuration()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save cycle: " + e.getMessage()));
        }
    }
    @GetMapping("/user")
    public ResponseEntity<?> getCyclesByUser(@RequestParam Long userId) {

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for id=" + userId));
        }

        // Fetch cycles ordered by most recent first
        var cycles = cycleRepository.findByUserIdOrderByStartDateDesc(userId);

        return ResponseEntity.ok(cycles);
    }

}
