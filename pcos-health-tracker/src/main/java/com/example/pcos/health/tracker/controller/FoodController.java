package com.example.pcos.health.tracker.controller;

import com.example.pcos.health.tracker.entity.Food;
import com.example.pcos.health.tracker.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    private FoodRepository foodRepository;

    // 1. Get ALL foods
    @GetMapping("/all")
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    // 2. Search by name
    @GetMapping("/search")
    public List<Food> searchByName(@RequestParam String name) {
        return foodRepository.findByNameContainingIgnoreCase(name);
    }

    // 3. Filter by category
    @GetMapping("/category")
    public List<Food> filterByCategory(@RequestParam String category) {
        return foodRepository.findByCategoryIgnoreCase(category);
    }

    // 4. Filter by PCOS Tag
    @GetMapping("/pcos-tag")
    public List<Food> filterByPcosTag(@RequestParam String tag) {
        return foodRepository.findByPcosTagIgnoreCase(tag);
    }
}
