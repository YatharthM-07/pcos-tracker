package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.DailySymptom;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;
import com.example.pcos.health.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/daily-log")
public class DailyLogController {

    @Autowired
    private DailySymptomRepository dailySymptomRepository;

    @Autowired
    private UserRepository userRepository; // ensure you have this

    public static class DailySymptomPayload {
        public Long userId;
        public String date; // accept YYYY-MM-DD
        public Integer cramps;
        public Integer acne;
        public Integer mood;
        public Integer bloating;
        public Integer fatigue;
        public Integer headache;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addOrUpdateDailyLog(@RequestBody DailySymptomPayload payload) {
        // Basic payload checks
        if (payload == null || payload.userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }

        LocalDate localDate;
        try {
            if (payload.date == null || payload.date.isBlank()) {
                localDate = LocalDate.now(); // default to today
            } else {
                localDate = LocalDate.parse(payload.date); // expects YYYY-MM-DD
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use YYYY-MM-DD"));
        }

        // Check user exists (avoid FK constraint errors)
        Optional<User> maybeUser = userRepository.findById(payload.userId);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for id=" + payload.userId));
        }
        User user = maybeUser.get();

        // Fetch existing log for this user+date (upsert logic)
        DailySymptom existing = dailySymptomRepository.findByUserIdAndDate(payload.userId, localDate);
        DailySymptom log;
        if (existing != null) {
            log = existing;
        } else {
            log = new DailySymptom();
            log.setDate(localDate);
            log.setUser(user);
        }

        // Set values with null-checks; repository/entity will clamp 0-10
        if (payload.cramps != null) log.setCramps(payload.cramps);
        if (payload.acne != null) log.setAcne(payload.acne);
        if (payload.mood != null) log.setMood(payload.mood);
        if (payload.bloating != null) log.setBloating(payload.bloating);
        if (payload.fatigue != null) log.setFatigue(payload.fatigue);
        if (payload.headache != null) log.setHeadache(payload.headache);

        try {
            DailySymptom saved = dailySymptomRepository.save(log);
            return ResponseEntity.ok(Map.of(
                    "message", "Daily log saved",
                    "id", saved.getId(),
                    "date", saved.getDate().toString()
            ));
        } catch (DataIntegrityViolationException dive) {
            // likely FK violation or constraint
            return ResponseEntity.status(500).body(Map.of("error", "Database error: " + dive.getRootCause().getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error: " + ex.getMessage()));
        }
    }
}
