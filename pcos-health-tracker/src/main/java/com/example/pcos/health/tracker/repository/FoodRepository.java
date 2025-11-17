package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByNameContainingIgnoreCase(String name);

    List<Food> findByCategoryIgnoreCase(String category);

    List<Food> findByPcosTagIgnoreCase(String tag);
}
