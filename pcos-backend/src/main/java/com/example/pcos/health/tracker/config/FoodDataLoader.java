package com.example.pcos.health.tracker.config;

import com.example.pcos.health.tracker.entity.Food;
import com.example.pcos.health.tracker.repository.FoodRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class FoodDataLoader implements CommandLineRunner {

    private final FoodRepository foodRepository;

    public FoodDataLoader(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // 🔒 Prevent duplicates on redeploy
        if (foodRepository.count() > 0) {
            System.out.println("🟡 Food table already populated. Skipping CSV import.");
            return;
        }

        Reader reader = new InputStreamReader(
                new ClassPathResource("data/foods.csv").getInputStream()
        );

        CSVParser csvParser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withTrim()
                .parse(reader);

        for (CSVRecord record : csvParser) {

            String name = record.get(0);
            String category = record.get(1);
            String tag = record.get(2).toUpperCase();
            String description = record.size() > 3 ? record.get(3) : "";

            Food food = new Food();
            food.setName(name);
            food.setCategory(category);
            food.setPcosTag(tag);
            food.setDescription(description);

            foodRepository.save(food);
        }

        System.out.println("✅ Food CSV imported successfully into PostgreSQL");
    }
}
