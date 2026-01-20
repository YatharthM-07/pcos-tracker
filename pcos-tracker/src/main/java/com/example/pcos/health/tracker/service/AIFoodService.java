package com.example.pcos.health.tracker.service;

import com.example.pcos.health.tracker.ai.gemini.GeminiRequest;
import com.example.pcos.health.tracker.ai.gemini.GeminiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AIFoodService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    // ✅ Let Spring manage WebClient (clean & testable)
    public AIFoodService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // ---------------------------------------------------------
    // ⭐ AI Food Recommendation (Gemini Flash)
    // ---------------------------------------------------------
    public String getFoodRecommendations(String preference) {

        String prompt = """
                You are a PCOS-friendly nutrition assistant.

                The user is looking for food suggestions based on:
                "%s"

                Respond in this format:

                Recommended foods:
                - Food 1
                - Food 2
                - Food 3

                Why they help:
                - Short reason 1
                - Short reason 2

                Food to avoid:
                - One food + short reason

                Keep it short, friendly, practical, and easy to understand.
                """.formatted(preference);

        String fullUrl = apiUrl + "?key=" + apiKey;

        try {
            GeminiResponse response = webClient.post()
                    .uri(fullUrl)
                    .bodyValue(new GeminiRequest(prompt))
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            // 🛡️ FULL null & safety checks (VERY IMPORTANT)
            if (response == null
                    || response.candidates == null
                    || response.candidates.isEmpty()
                    || response.candidates.get(0).content == null
                    || response.candidates.get(0).content.parts == null
                    || response.candidates.get(0).content.parts.isEmpty()
                    || response.candidates.get(0).content.parts.get(0).text == null) {

                return defaultFallbackMessage();
            }

            return response.candidates.get(0).content.parts.get(0).text;

        } catch (Exception e) {
            System.out.println("❌ Gemini Food AI Error: " + e.getMessage());
            return defaultFallbackMessage();
        }
    }

    // ---------------------------------------------------------
    // 🛡️ Safe fallback (used when AI fails)
    // ---------------------------------------------------------
    private String defaultFallbackMessage() {
        return """
                Recommended foods:
                - Vegetables
                - Whole grains
                - Pulses and legumes

                Why they help:
                - Improve insulin sensitivity
                - Support hormonal balance

                Food to avoid:
                - Sugary and highly processed foods (can worsen PCOS symptoms)
                """;
    }
}
