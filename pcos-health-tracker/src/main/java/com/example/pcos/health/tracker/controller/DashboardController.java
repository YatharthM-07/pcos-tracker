package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.security.AuthContext;
import com.example.pcos.health.tracker.service.AIDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AIDashboardService aiDashboardService;

    @Autowired
    private AuthContext authContext;

    // ================================================================
    // 1️⃣ BASIC DASHBOARD SUMMARY (No AI)
    // ================================================================
    @GetMapping("/summary")
    public ResponseEntity<?> getBasicSummary() {
        Long userId = authContext.getCurrentUser().getId();
        Map<String, Object> summary = aiDashboardService.generateBasicSummary(userId);

        return ResponseEntity.ok(summary);
    }

    // ================================================================
    // 2️⃣ AI-ENHANCED SUMMARY (Gemini AI)
    // ================================================================
    @GetMapping("/ai-summary")
    public ResponseEntity<?> getAiSummary() {

        System.out.println("➡️ CONTROLLER CALL RECEIVED");

        System.out.println("Service instance = " + aiDashboardService);

        Long userId = authContext.getCurrentUser().getId();

        String aiSummary = aiDashboardService.generateAISummary(userId);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "aiSummary", aiSummary
        ));
    }

}
