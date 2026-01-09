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
        if (!cycles.isEmpty()) {
            double avg = cycles.stream().mapToInt(c -> c.getDuration()).average().orElse(0);
            summary.put("avgCycleLength", Math.round(avg));
            summary.put("lastCycleLength", cycles.get(0).getDuration());
        }

        var logs = logRepo.findByUserId(userId);
        summary.put("totalLogs", logs.size());

        return summary;
    }

    public String generateAISummary(Long userId) {

        System.out.println("➡️ AI DASHBOARD SUMMARY METHOD CALLED");

        Map<String, Object> basic = generateBasicSummary(userId);

        String prompt = """
                You are a supportive PCOS wellness assistant.
                Here is the user's tracking summary:
                %s

                Write a very clear, supportive 3–4 sentence summary
                focusing on cycle patterns, symptoms, and encouragement.
                """.formatted(basic);

        String finalUrl = apiUrl + "?key=" + apiKey;

        System.out.println("➡️ FINAL URL = " + finalUrl);
        System.out.println("➡️ PROMPT = " + prompt);

        try {
            GeminiResponse response = webClient.post()
                    .uri(finalUrl)
                    .bodyValue(new GeminiRequest(prompt))
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            return response.candidates.get(0).content.parts.get(0).text;

        } catch (Exception e) {
            System.out.println("❌ GEMINI ERROR: " + e.getMessage());
            return "AI summary unavailable.";
        }
    }
}
