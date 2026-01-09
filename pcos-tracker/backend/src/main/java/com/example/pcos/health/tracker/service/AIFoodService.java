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

    private final WebClient webClient = WebClient.create();

    // ---------------------------------------------------------
    // ⭐ AI Food Recommendation (Gemini Flash)
    // ---------------------------------------------------------
    public String getFoodRecommendations(String preference) {

        String prompt = """
                You are a PCOS-friendly nutrition assistant.

                The user is looking for food suggestions based on:
                "%s"

                Return:
                - 3 recommended foods
                - 1–2 short reasons why they are helpful for PCOS
                - 1 food they should avoid, with a simple explanation

                Keep it short, friendly, and practical.
                """.formatted(preference);

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
            System.out.println("❌ Food AI Error: " + e.getMessage());
            return "Try focusing on whole, fiber-rich foods and reducing sugary or processed items.";
        }
    }
}
