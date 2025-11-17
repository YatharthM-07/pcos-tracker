package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.DailySymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailySymptomRepository extends JpaRepository<DailySymptom, Long> {

    // Get all logs of a user
    List<DailySymptom> findByUserId(Long userId);

    // Get log for a specific date
    DailySymptom findByUserIdAndDate(Long userId, LocalDate date);

    // NEW — Get the latest daily log (this is what your controller needs)
    DailySymptom findTopByUserIdOrderByDateDesc(Long userId);
}
