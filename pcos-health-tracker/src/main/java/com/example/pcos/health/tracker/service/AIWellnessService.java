package com.example.pcos.health.tracker.service;

import com.example.pcos.health.tracker.ai.gemini.GeminiRequest;
import com.example.pcos.health.tracker.ai.gemini.GeminiResponse;
import com.example.pcos.health.tracker.entity.DailySymptom;
import com.example.pcos.health.tracker.repository.DailySymptomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Service
public class AIWellnessService {

    @Autowired
    private DailySymptomRepository dailySymptomRepository;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();

    // ---------------------------------------------------------
    // ⭐ Generate Wellness Message using Gemini Flash
    // ---------------------------------------------------------
    public String generateWellnessMessage(Long userId) {

        // Fetch today's log (or last available)
        DailySymptom log = dailySymptomRepository
                .findTopByUserIdOrderByDateDesc(userId);

        if (log == null) {
            return "You're doing great by checking in — start logging daily to understand your patterns better!";
        }

        String prompt = """
                You are a supportive PCOS wellness assistant.
                The user logged these symptoms today:

                Cramps: %d
                Acne: %d
                Mood: %d
                Bloating: %d
                Fatigue: %d
                Headache: %d

                Write a warm, uplifting wellness message in 2–3 sentences.
                Focus on encouragement, self-care, and emotional support.
                """.formatted(
                log.getCramps(),
                log.getAcne(),
                log.getMood(),
                log.getBloating(),
                log.getFatigue(),
                log.getHeadache()
        );

        String fullUrl = apiUrl + "?key=" + apiKey;

        try {
            GeminiResponse response = webClient.post()
                    .uri(fullUrl)
                    .bodyValue(new GeminiRequest(prompt))
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            return response.candidates.get(0).content.parts.get(0).text;

        } catch (Exception e) {
            System.out.println("❌ Wellness AI Error: " + e.getMessage());
            return "Remember to take it slow today — small self-care moments can make a big difference.";
        }
    }
}
