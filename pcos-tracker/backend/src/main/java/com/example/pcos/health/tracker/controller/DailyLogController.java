package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.DailySymptom;
import com.example.pcos.health.tracker.entity.User;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;
import com.example.pcos.health.tracker.security.AuthContext;
import com.example.pcos.health.tracker.service.AIWellnessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/daily-log")
public class DailyLogController {

    @Autowired
    private DailySymptomRepository dailySymptomRepository;

    @Autowired
    private AuthContext authContext;

    @Autowired
    private AIWellnessService aiWellnessService;

    // -----------------------------------------------------------
    // Payload
    // -----------------------------------------------------------
    public static class DailyLogPayload {
        public String date;
        public Integer cramps;
        public Integer acne;
        public Integer mood;
        public Integer bloating;
        public Integer fatigue;
        public Integer headache;
    }

    // -----------------------------------------------------------
    // 1️⃣ ADD OR UPDATE LOG (ONE PER DAY) + AI
    // -----------------------------------------------------------
    @PostMapping("/add")
    public ResponseEntity<?> addDailyLog(@RequestBody DailyLogPayload payload) {

        User currentUser = authContext.getCurrentUser();

        LocalDate logDate = (payload.date != null && !payload.date.isBlank())
                ? LocalDate.parse(payload.date)
                : LocalDate.now();

        // 🔁 Update if exists, else create
        DailySymptom log = dailySymptomRepository
                .findByUserIdAndDate(currentUser.getId(), logDate);

        if (log == null) {
            log = new DailySymptom();
            log.setUser(currentUser);
            log.setDate(logDate);
        }

        if (payload.cramps != null) log.setCramps(payload.cramps);
        if (payload.acne != null) log.setAcne(payload.acne);
        if (payload.mood != null) log.setMood(payload.mood);
        if (payload.bloating != null) log.setBloating(payload.bloating);
        if (payload.fatigue != null) log.setFatigue(payload.fatigue);
        if (payload.headache != null) log.setHeadache(payload.headache);

        dailySymptomRepository.save(log);

        String aiMessage = aiWellnessService.generateWellnessMessage(currentUser.getId());

// ⭐ Store AI message in DB (IMPORTANT)
        log.setWellnessMessage(aiMessage);

// ⭐ Save (INSERT or UPDATE for the day)
        dailySymptomRepository.save(log);

        return ResponseEntity.ok(Map.of(
                "message", "Daily log saved successfully",
                "date", logDate.toString(),
                "wellnessMessage", aiMessage
        ));
    }

    // -----------------------------------------------------------
    // 2️⃣ TODAY'S LOG (READ ONLY)
    // -----------------------------------------------------------
    @GetMapping("/today")
    public ResponseEntity<?> getTodayLog() {

        User currentUser = authContext.getCurrentUser();
        LocalDate today = LocalDate.now();

        DailySymptom log = dailySymptomRepository
                .findByUserIdAndDate(currentUser.getId(), today);

        if (log == null) {
            return ResponseEntity.ok(Map.of(
                    "exists", false
            ));
        }

        return ResponseEntity.ok(Map.of(
                "exists", true,
                "log", Map.of(
                        "cramps", log.getCramps(),
                        "acne", log.getAcne(),
                        "mood", log.getMood(),
                        "bloating", log.getBloating(),
                        "fatigue", log.getFatigue(),
                        "headache", log.getHeadache()
                )
        ));

    }


    // -----------------------------------------------------------
    // 3️⃣ LOG BY DATE
    // -----------------------------------------------------------
    @GetMapping("/by-date")
    public ResponseEntity<?> getLogByDate(@RequestParam String date) {

        User currentUser = authContext.getCurrentUser();

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid date format. Use YYYY-MM-DD"
            ));
        }

        DailySymptom log =
                dailySymptomRepository.findByUserIdAndDate(currentUser.getId(), parsedDate);

        if (log == null) {
            return ResponseEntity.ok(Map.of(
                    "message", "No log found for this date"
            ));
        }

        return ResponseEntity.ok(log);
    }

    // -----------------------------------------------------------
    // 4️⃣ ALL LOGS (HISTORY)
    // -----------------------------------------------------------
    @GetMapping("/all")
    public ResponseEntity<?> getAllLogs() {

        User currentUser = authContext.getCurrentUser();

        List<DailySymptom> logs =
                dailySymptomRepository.findByUserIdOrderByDateDesc(currentUser.getId());

        return ResponseEntity.ok(logs);
    }
}
