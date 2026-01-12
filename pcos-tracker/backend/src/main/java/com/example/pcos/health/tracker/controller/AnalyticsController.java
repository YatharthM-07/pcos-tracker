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
    public double averageCycleLength() {
        List<Cycle> cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId())
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Cycle::getEndDate).reversed())
                .toList();

        if (cycles.isEmpty()) return 0;

        return cycles.stream()
                .limit(3) // last 3 completed cycles
                .mapToInt(Cycle::getDuration)
                .average()
                .orElse(0);

    }

    @GetMapping("/previous-period-length")
    public int previousPeriodLength() {
        List<Cycle> cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId())
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Cycle::getEndDate).reversed())
                .toList();

        if (cycles.isEmpty()) return 0;

        Cycle lastCompleted = cycles.get(0);
        return lastCompleted.getDuration();

    }

    @GetMapping("/next-period-in")
    public long nextPeriodIn() {

        List<Cycle> cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId())
                .stream()
                .filter(c -> c.getEndDate() != null &&
                        c.getEndDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Cycle::getEndDate).reversed())
                .toList();

        if (cycles.isEmpty()) return -1;

        double avg = averageCycleLength();
        if (avg <= 0) return -1;

        Cycle lastCompleted = cycles.get(0);

        LocalDate predictedNextStart =
                lastCompleted.getStartDate().plusDays(Math.round(avg));

        return ChronoUnit.DAYS.between(LocalDate.now(), predictedNextStart);
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

        List<Integer> durations = cycles.stream()
                .limit(3)
                .map(Cycle::getDuration)
                .toList();

        double mean = durations.stream().mapToInt(i -> i).average().orElse(0);
        if (mean == 0) return -1;

        double variance = durations.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average()
                .orElse(0);

        double cv = Math.sqrt(variance) / mean;
        return (int) Math.max(0, Math.min(100, (1 - cv) * 100));

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
        if (next <= 3) result.add("Your next period may start very soon.");
        else result.add("Your next cycle is expected in " + next + " days.");

        Map<String, Object> most = mostProblematic();
        if (most.containsKey("mostProblematic"))
            result.add("Most common symptom: " + most.get("mostProblematic"));

        return result;
    }

    // MAIN FRONTEND ENDPOINT
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        return Map.of(
                "averageCycleLength", averageCycleLength(),
                "previousPeriodLength", previousPeriodLength(),
                "nextPeriodIn", nextPeriodIn(),
                "regularityScore", regularityScore(),
                "mostProblematicSymptom", mostProblematic(),
                "wellnessMessage", wellnessMessage().get("message"),
                "insights", insights()
        );
    }
}
