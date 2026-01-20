package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.DailySymptom;
import com.example.pcos.health.tracker.repository.CycleRepository;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;
import com.example.pcos.health.tracker.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private CycleRepository cycleRepo;

    @Autowired
    private DailySymptomRepository dailyRepo;

    @Autowired
    private AuthContext authContext;

    private Long userId() {
        return authContext.getCurrentUser().getId();
    }

    @GetMapping("/average-cycle-length")
    public long averageCycleLength() {
        List<Long> cycles = cycleLengthsBetweenPeriods();
        if (cycles.isEmpty()) return 0;

        return Math.round(
                cycles.stream().mapToLong(Long::longValue).average().orElse(0)
        );
    }

    @GetMapping("/previous-period-length")
    public int previousPeriodLength() {
        return cycleRepo.findByUserIdOrderByStartDateDesc(userId())
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().isBefore(LocalDate.now()))
                .findFirst()
                .map(c ->
                        (int) ChronoUnit.DAYS.between(
                                c.getStartDate(),
                                c.getEndDate()
                        ) + 1
                )
                .orElse(0);
    }




    @GetMapping("/next-period-in")
    public long nextPeriodIn() {
        List<Cycle> cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId());
        if (cycles.isEmpty()) return -1;

        long avgCycle = averageCycleLength();
        if (avgCycle == 0) return -1;

        LocalDate lastStart = cycles.get(0).getStartDate();
        LocalDate expectedNext = lastStart.plusDays(avgCycle);

        return ChronoUnit.DAYS.between(LocalDate.now(), expectedNext);
    }



    @GetMapping("/regularity-score")
    public int regularityScore() {
        List<Cycle> cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId())
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Cycle::getEndDate).reversed())
                .toList();

        if (cycles.size() < 2) return -1;

        List<Long> lengths = cycleLengthsBetweenPeriods();
        if (lengths.size() < 2) return -1;

        double mean = lengths.stream().mapToLong(l -> l).average().orElse(0);
        double variance = lengths.stream()
                .mapToDouble(l -> Math.pow(l - mean, 2))
                .average()
                .orElse(0);

        double cv = Math.sqrt(variance) / mean;
        return (int) Math.max(0, Math.min(100, (1 - cv) * 100));

    }
    private List<Long> cycleLengthsBetweenPeriods() {
        List<Cycle> cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId());

        if (cycles.size() < 2) return List.of();

        List<Long> lengths = new ArrayList<>();

        for (int i = 0; i < cycles.size() - 1; i++) {
            LocalDate current = cycles.get(i).getStartDate();
            LocalDate previous = cycles.get(i + 1).getStartDate();

            long days = ChronoUnit.DAYS.between(previous, current);
            if (days > 0) lengths.add(days);
        }

        return lengths;
    }


    @GetMapping("/symptoms/most-problematic")
    public Map<String, Object> mostProblematic() {

        List<DailySymptom> logs = dailyRepo.findByUserId(userId());
        if (logs.isEmpty()) return Map.of("message", "No symptoms recorded.");

        Map<String, Integer> totals = new HashMap<>();

        totals.put("cramps", logs.stream().mapToInt(DailySymptom::getCramps).sum());
        totals.put("acne", logs.stream().mapToInt(DailySymptom::getAcne).sum());
        totals.put("mood", logs.stream().mapToInt(DailySymptom::getMood).sum());
        totals.put("bloating", logs.stream().mapToInt(DailySymptom::getBloating).sum());
        totals.put("fatigue", logs.stream().mapToInt(DailySymptom::getFatigue).sum());
        totals.put("headache", logs.stream().mapToInt(DailySymptom::getHeadache).sum());

        String worst = Collections.max(totals.entrySet(),
                Map.Entry.comparingByValue()).getKey();

        return Map.of("mostProblematic", worst, "scores", totals);
    }


    @GetMapping("/wellness-message")
    public Map<String, String> wellnessMessage() {

        DailySymptom log = dailyRepo.findTopByUserIdOrderByDateDesc(userId());
        if (log == null)
            return Map.of("message", "Start logging symptoms to see wellness insights.");

        int mood = log.getMood();
        int cramps = log.getCramps();
        int acne = log.getAcne();

        String msg = "You're doing great. Keep tracking!";

        if (mood <= 3) msg = "Feeling low? It's okay to rest today.";
        if (cramps >= 7) msg = "Strong cramps today — take it slow.";
        if (acne >= 7) msg = "Skin breakout — likely hormonal. Be gentle with yourself.";
        if (mood >= 6 && cramps <= 3 && acne <= 3)
            msg = "Your body seems at ease today. Great job!";

        return Map.of("message", msg);
    }


    @GetMapping("/insights")
    public List<String> insights() {

        List<String> result = new ArrayList<>();

        double avg = averageCycleLength();
        result.add("Your average cycle length is " + Math.round(avg) + " days.");

        int reg = regularityScore();
        if (reg >= 80) result.add("Your cycles are very regular.");
        else if (reg >= 50) result.add("Moderate cycle regularity.");
        else result.add("Your cycles look irregular — keep logging.");

        long next = nextPeriodIn();

        if (next < 0) {
            result.add(
                    "Your cycle appears delayed by about " + Math.abs(next) +
                            " days. Irregular timing is common with PCOS."
            );
        }
        else if (next <= 7) {
            result.add(
                    "Your next period may begin soon. Listen to your body and rest if needed."
            );
        }
        else if (next <= 45) {
            result.add(
                    "Your next period is likely in about " + next + " days."
            );
        }
        else {
            result.add(
                    "Your cycle length is currently longer than average. This can happen with PCOS."
            );
        }



        Map<String, Object> most = mostProblematic();
        if (most.containsKey("mostProblematic"))
            result.add("Most common symptom: " + most.get("mostProblematic"));

        return result;
    }

    // MAIN FRONTEND ENDPOINT
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        long avgCycle = averageCycleLength();
        long next = nextPeriodIn();

        return Map.of(
                "averageCycleLength", avgCycle,
                "previousPeriodLength", previousPeriodLength(),
                "nextPeriodIn", next,
                "regularityScore", regularityScore(),
                "insights", insights()
        );
    }
}
