package com.example.pcos.health.tracker.ai.gemini;

import java.util.List;

public class GeminiRequest {

    public List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = List.of(
                new Content(List.of(new Part(text)))
        );
    }

    public static class Content {
        public List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
