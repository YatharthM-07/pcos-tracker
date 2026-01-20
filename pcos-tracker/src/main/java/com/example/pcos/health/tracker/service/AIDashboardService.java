package com.example.pcos.health.tracker.service;

import com.example.pcos.health.tracker.ai.gemini.GeminiRequest;
import com.example.pcos.health.tracker.ai.gemini.GeminiResponse;
import com.example.pcos.health.tracker.repository.CycleRepository;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class AIDashboardService {

    private final CycleRepository cycleRepo;
    private final DailySymptomRepository logRepo;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();

    public AIDashboardService(CycleRepository cycleRepo, DailySymptomRepository logRepo) {
        this.cycleRepo = cycleRepo;
        this.logRepo = logRepo;
    }

    public Map<String, Object> generateBasicSummary(Long userId) {

        Map<String, Object> summary = new HashMap<>();

        var cycles = cycleRepo.findByUserIdOrderByStartDateDesc(userId);
        var logs = logRepo.findByUserId(userId);

        summary.put("totalLogs", logs.size());

        if (!cycles.isEmpty()) {
            double avg = cycles.stream().mapToInt(c -> c.getDuration()).average().orElse(0);
            int avgCycle = (int) Math.round(avg);

            summary.put("avgCycleLength", avgCycle);
            summary.put("lastCycleLength", cycles.get(0).getDuration());

            // 👇 KEY INSIGHT FLAGS
            summary.put("cycleDataIncomplete", avgCycle < 21);
            summary.put("likelyPCOSPattern", avgCycle > 35 || cycles.size() < 4);
        }

        // symptom dominance
        if (!logs.isEmpty()) {
            int fatigueAvg = (int) logs.stream().mapToInt(l -> l.getFatigue()).average().orElse(0);
            int acneAvg = (int) logs.stream().mapToInt(l -> l.getAcne()).average().orElse(0);

            if (fatigueAvg >= acneAvg && fatigueAvg >= 6) {
                summary.put("dominantSymptom", "fatigue");
            } else if (acneAvg >= 6) {
                summary.put("dominantSymptom", "acne");
            }
        }

        return summary;
    }


    public String generateAISummary(Long userId) {

        System.out.println("➡️ AI DASHBOARD SUMMARY METHOD CALLED");

        Map<String, Object> basic = generateBasicSummary(userId);

        String prompt = """
You are Maitri, a gentle PCOS/PCOD wellness companion.

The user has PCOS/PCOD. Write a warm, reassuring message that helps them
understand their body and feel supported.

Context:
%s

Guidelines:
- Explain that irregular or confusing cycle patterns are very common in PCOS/PCOD.
- Clarify that short recorded cycle lengths often reflect bleeding days, not the full cycle gap.
- Reassure the user that this does NOT mean something is wrong.
- Briefly mention that symptoms like fatigue, acne, or bloating can appear before cycles feel regular.
- Gently suggest 2–3 lifestyle supports such as balanced meals, light movement, sleep, or stress care.
- Phrase suggestions as help, not instructions.
- Keep the tone calm, kind, and human.
- Write 4–5 short sentences.
- Avoid medical or clinical words (no diagnosis, assessment, imbalance, disorder).
""".formatted(basic);




        String finalUrl = apiUrl + "?key=" + apiKey;

        System.out.println("➡️ FINAL URL = " + finalUrl);
        System.out.println("➡️ PROMPT = " + prompt);

        try {
            GeminiResponse response = webClient.post()
                    .uri(finalUrl)
                    .header("Content-Type", "application/json") // ✅ REQUIRED
                    .bodyValue(new GeminiRequest(prompt))
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            // ✅ SAFE RESPONSE HANDLING
            if (response == null ||
                    response.candidates == null ||
                    response.candidates.isEmpty() ||
                    response.candidates.get(0).content == null ||
                    response.candidates.get(0).content.parts == null ||
                    response.candidates.get(0).content.parts.isEmpty()) {

                return "You're doing a great job tracking your health. Keep observing your cycles and symptoms — consistency brings clarity 💙";
            }

            return response.candidates.get(0).content.parts.get(0).text;

        } catch (Exception e) {
            System.out.println("❌ GEMINI ERROR: " + e.getMessage());
            return "AI insights are temporarily unavailable, but your tracking progress looks solid 🌱";
        }
    }

}
