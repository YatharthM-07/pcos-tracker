package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Symptom;
import com.example.pcos.health.tracker.entity.DailySymptom;
import com.example.pcos.health.tracker.repository.SymptomRepository;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;
import com.example.pcos.health.tracker.security.AuthContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/symptoms")
public class SymptomController {

    @Autowired
    private SymptomRepository symptomRepository;

    // ⭐ NEW (already exists in project)
    @Autowired
    private DailySymptomRepository dailySymptomRepository;

    @Autowired
    private AuthContext authContext;

    /* ==========================
       EXISTING ENDPOINTS (UNCHANGED)
    ========================== */

    @PostMapping("/add")
    public String addSymptom(@RequestBody Symptom symptom) {
        symptomRepository.save(symptom);
        return "Symptom added successfully!";
    }

    @GetMapping("/all")
    public List<Symptom> getAllSymptoms() {
        return symptomRepository.findAll();
    }

    /* ==========================
       NEW ENDPOINT 1: FREQUENCY
       (For Bar Chart)
    ========================== */

    @GetMapping("/frequency")
    public Map<String, Integer> symptomFrequency() {

        Long userId = authContext.getCurrentUser().getId();
        List<DailySymptom> logs = dailySymptomRepository.findByUserId(userId);

        Map<String, Integer> freq = new LinkedHashMap<>();
        freq.put("Cramps", logs.stream().mapToInt(DailySymptom::getCramps).sum());
        freq.put("Fatigue", logs.stream().mapToInt(DailySymptom::getFatigue).sum());
        freq.put("Acne", logs.stream().mapToInt(DailySymptom::getAcne).sum());
        freq.put("Mood", logs.stream().mapToInt(DailySymptom::getMood).sum());
        freq.put("Bloating", logs.stream().mapToInt(DailySymptom::getBloating).sum());
        freq.put("Headache", logs.stream().mapToInt(DailySymptom::getHeadache).sum());

        return freq;
    }

    /* ==========================
       NEW ENDPOINT 2: TREND
       (For Line Chart)
    ========================== */

    @GetMapping("/trend")
    public List<Map<String, Object>> symptomTrend(
            @RequestParam(defaultValue = "7") int days
    ) {
        Long userId = authContext.getCurrentUser().getId();
        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        List<DailySymptom> logs =
                dailySymptomRepository
                        .findByUserIdAndDateAfterOrderByDateAsc(userId, startDate);

        return logs.stream()
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", log.getDate().toString());
                    map.put("fatigue", log.getFatigue());
                    return map;
                })
                .toList();

    }
}
