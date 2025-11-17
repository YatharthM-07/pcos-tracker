package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Cycle;
import com.example.pcos.health.tracker.entity.DailySymptom;
import com.example.pcos.health.tracker.repository.CycleRepository;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private CycleRepository cycleRepository;

    @Autowired
    private DailySymptomRepository dailySymptomRepository;

    // --------------------------------------------------------
    // 1) AVERAGE CYCLE LENGTH
    // --------------------------------------------------------
    @GetMapping("/average-cycle-length")
    public double averageCycleLength() {

        List<Cycle> cycles = cycleRepository.findAll();

        if (cycles.size() < 2) return 0;

        int total = 0;
        for (Cycle c : cycles) total += c.getDuration();

        return total / (double) cycles.size();
    }

    // --------------------------------------------------------
    // 2) PREVIOUS PERIOD LENGTH
    // --------------------------------------------------------
    @GetMapping("/previous-period-length")
    public int previousPeriodLength() {

        List<Cycle> cycles = cycleRepository.findAll();

        if (cycles.isEmpty()) return 0;

        Cycle lastCycle = cycles.get(cycles.size() - 1);

        if (lastCycle.getStartDate() == null || lastCycle.getEndDate() == null)
            return 0;

        return (int) java.time.temporal.ChronoUnit.DAYS.between(
                lastCycle.getStartDate(),
                lastCycle.getEndDate()
        );
    }

    // --------------------------------------------------------
    // 3) NEXT PERIOD PREDICTION
    // --------------------------------------------------------
    @GetMapping("/next-period-in")
    public long nextPeriodIn() {

        List<Cycle> cycles = cycleRepository.findAll();
        if (cycles.isEmpty()) return -1;

        Cycle lastCycle = cycles.get(cycles.size() - 1);

        double avg = averageCycleLength();
        if (avg == 0) return -1;

        java.time.LocalDate expected =
                lastCycle.getStartDate().plusDays((long) avg);

        return java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(),
                expected
        );
    }

    // --------------------------------------------------------
    // 4) REGULARITY SCORE
    // --------------------------------------------------------
    @GetMapping("/regularity-score")
    public int regularityScore() {

        List<Cycle> cycles = cycleRepository.findAll();
        if (cycles.size() < 2) return -1;

        double total = 0;
        for (Cycle c : cycles) total += c.getDuration();

        double mean = total / cycles.size();
        if (mean == 0) return -1;

        double variance = 0;
        for (Cycle c : cycles)
            variance += Math.pow(c.getDuration() - mean, 2);

        double stddev = Math.sqrt(variance / cycles.size());

        double cv = stddev / mean;

        double score = (1 - cv) * 100;
        return (int) Math.max(0, Math.min(100, score));
    }

    // --------------------------------------------------------
    // 5) MOST PROBLEMATIC SYMPTOM
    // --------------------------------------------------------
    // --------------------------------------------------------
// 5) MOST PROBLEMATIC SYMPTOM (UPDATED - ALL 6 SYMPTOMS)
// --------------------------------------------------------
    @GetMapping("/symptoms/most-problematic")
    public Map<String, Object> mostProblematic(@RequestParam Long userId) {

        List<DailySymptom> logs = dailySymptomRepository.findByUserId(userId);

        if (logs.isEmpty()) {
            return Map.of("message", "No symptom data found");
        }

        // Sum all symptoms
        int cramps = 0;
        int acne = 0;
        int mood = 0;
        int bloating = 0;
        int fatigue = 0;
        int headache = 0;

        for (DailySymptom log : logs) {
            cramps += log.getCramps();
            acne += log.getAcne();
            mood += log.getMood();
            bloating += log.getBloating();
            fatigue += log.getFatigue();
            headache += log.getHeadache();
        }

        // Put all symptom totals in a map
        Map<String, Integer> scores = new HashMap<>();
        scores.put("cramps", cramps);
        scores.put("acne", acne);
        scores.put("mood", mood);
        scores.put("bloating", bloating);
        scores.put("fatigue", fatigue);
        scores.put("headache", headache);

        // Find highest
        String worst = Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey();

        return Map.of(
                "userId", userId,
                "mostProblematicSymptom", worst,
                "scores", scores
        );
    }


    // --------------------------------------------------------
    // 6) WELLNESS MESSAGE BASED ON DAILY LOG
    // --------------------------------------------------------
    @GetMapping("/wellness-message")
    public Map<String, String> wellnessMessage(@RequestParam Long userId) {

        DailySymptom log = dailySymptomRepository
                .findTopByUserIdOrderByDateDesc(userId);

        if (log == null) {
            return Map.of("message",
                    "Start logging your day — small check-ins help you understand your body better."
            );
        }

        int cramps = log.getCramps();
        int acne = log.getAcne();
        int mood = log.getMood();

        String message = "You're showing up for yourself — even small steps matter.";

        if (mood <= 3) {
            message = "Your emotions matter. It's okay to feel low — you're doing your best.";
        } else if (mood <= 6) {
            message = "Take a small pause today. A deep breath can make things a bit lighter.";
        } else {
            message = "You're emotionally stronger today — keep nurturing yourself.";
        }

        if (cramps >= 7) {
            message = "You're experiencing strong cramps today. Be gentle with yourself — rest and warmth can help.";
        } else if (cramps >= 4) {
            message = "A little discomfort today — listen to your body and pace yourself.";
        }

        if (acne >= 7) {
            message = "Your skin may be acting up today — it’s normal with hormonal shifts. You're still glowing.";
        }

        if (cramps <= 3 && acne <= 3 && mood >= 4) {
            message = "Your body seems more at ease today — keep up your healthy habits.";
        }

        return Map.of("message", message);
    }

    // --------------------------------------------------------
    // 7) INSIGHTS (NEW ENDPOINT)
    // --------------------------------------------------------
    @GetMapping("/insights")
    public List<String> insights(@RequestParam Long userId) {

        List<String> insights = new ArrayList<>();

        List<Cycle> cycles = cycleRepository.findByUserIdOrderByStartDateDesc(userId);
        if (cycles == null || cycles.isEmpty()) {
            insights.add("Add your cycle details to start getting personalized insights.");
            return insights;
        }

        List<Integer> durations = new ArrayList<>();
        for (Cycle c : cycles) durations.add(c.getDuration());

        double avg = durations.stream().mapToDouble(d -> d).average().orElse(0);
        insights.add("Your average cycle length is " + Math.round(avg) + " days.");

        if (durations.size() >= 2) {
            double mean = avg;
            double variance = 0;

            for (int d : durations)
                variance += Math.pow(d - mean, 2);

            double stddev = Math.sqrt(variance / durations.size());
            double cv = (mean == 0) ? 0 : (stddev / mean) * 100;

            if (cv < 20) insights.add("Your cycles are highly regular.");
            else if (cv < 40) insights.add("Your cycles show moderate regularity.");
            else insights.add("Your cycles look a bit irregular — keep tracking consistently.");
        }

        if (durations.size() >= 3) {
            int a = durations.get(0);
            int b = durations.get(1);
            int c = durations.get(2);

            if (a > b && b > c) insights.add("Your cycle length trend is gradually increasing.");
            else if (a < b && b < c) insights.add("Your cycle lengths are gradually decreasing.");
            else insights.add("Your cycle lengths show mild fluctuations.");
        }

        Cycle last = cycles.get(0);
        java.time.LocalDate predicted = last.getStartDate().plusDays((long) avg);

        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(),
                predicted
        );

        if (daysUntil <= 3) insights.add("Your next cycle may start soon.");
        else if (daysUntil <= 10) insights.add("Your next cycle is expected in " + daysUntil + " days.");
        else insights.add("Your next cycle is not very near.");

        List<DailySymptom> logs = dailySymptomRepository.findByUserId(userId);

        if (!logs.isEmpty()) {
            int cramps = 0, acne = 0, mood = 0, bloating = 0, fatigue = 0, headache = 0;

            for (DailySymptom ds : logs) {
                cramps += ds.getCramps();
                acne += ds.getAcne();
                mood += ds.getMood();
                bloating += ds.getBloating();
                fatigue += ds.getFatigue();
                headache += ds.getHeadache();
            }

            Map<String, Integer> map = new HashMap<>();
            map.put("Cramps", cramps);
            map.put("Acne", acne);
            map.put("Mood", mood);
            map.put("Bloating", bloating);
            map.put("Fatigue", fatigue);
            map.put("Headache", headache);

            String worst = Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();
            insights.add(worst + " appears to be your most frequent symptom.");
        }

        return insights;
    }
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam Long userId) {

        Map<String, Object> data = new HashMap<>();

        // 1) Average cycle length
        data.put("averageCycleLength", averageCycleLength());

        // 2) Previous period length
        data.put("previousPeriodLength", previousPeriodLength());

        // 3) Next period prediction
        data.put("nextPeriodIn", nextPeriodIn());

        // 4) Regularity score
        data.put("regularityScore", regularityScore());

        // 5) Most problematic symptom
        Map<String, Object> mostProblematic = mostProblematic(userId);
        data.put("mostProblematicSymptom", mostProblematic);

        // 6) Wellness message
        Map<String, String> wellness = wellnessMessage(userId);
        data.put("wellnessMessage", wellness.get("message"));

        // 7) Insights
        List<String> insightsList = insights(userId);
        data.put("insights", insightsList);

        return data;
    }

}
