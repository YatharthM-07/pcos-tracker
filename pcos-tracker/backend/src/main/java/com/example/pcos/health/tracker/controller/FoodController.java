package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Food;
import com.example.pcos.health.tracker.repository.FoodRepository;
import com.example.pcos.health.tracker.security.AuthContext;
import com.example.pcos.health.tracker.service.AIFoodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private AIFoodService aiFoodService;

    @Autowired
    private AuthContext authContext;

    // -------------------------------------------------------------------
    // 1️⃣ Get ALL foods
    // -------------------------------------------------------------------
    @GetMapping("/all")
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    // -------------------------------------------------------------------
    // 2️⃣ Search food by name
    // -------------------------------------------------------------------
    @GetMapping("/search")
    public List<Food> searchByName(@RequestParam String name) {
        return foodRepository.findByNameContainingIgnoreCase(name);
    }

    // -------------------------------------------------------------------
    // 3️⃣ Get foods by category
    // -------------------------------------------------------------------
    @GetMapping("/category")
    public List<Food> filterByCategory(@RequestParam String category) {
        return foodRepository.findByCategoryIgnoreCase(category);
    }

    // -------------------------------------------------------------------
    // 4️⃣ Get foods by PCOS tag
    // -------------------------------------------------------------------
    @GetMapping("/pcos-tag")
    public List<Food> filterByPcosTag(@RequestParam String tag) {
        return foodRepository.findByPcosTagIgnoreCase(tag);
    }

    // -------------------------------------------------------------------
    // ⭐ 5️⃣ Gemini AI Food Recommendation
    // -------------------------------------------------------------------
    @GetMapping("/ai")
    public ResponseEntity<?> getAiFoodSuggestions(@RequestParam String preference) {

        String recommendations = aiFoodService.getFoodRecommendations(preference);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("preference", preference);
        response.put("aiRecommendations", recommendations);

        if (authContext.getCurrentUser() != null) {
            response.put("userId", authContext.getCurrentUser().getId());
        }

        return ResponseEntity.ok(response);
    }



}
