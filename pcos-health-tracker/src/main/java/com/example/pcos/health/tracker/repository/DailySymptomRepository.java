package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.DailySymptom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailySymptomRepository extends JpaRepository<DailySymptom, Long> {

    DailySymptom findByUserIdAndDate(Long userId, LocalDate date);

    List<DailySymptom> findByUserId(Long userId);

    List<DailySymptom> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate start, LocalDate end);

    DailySymptom findTopByUserIdOrderByDateDesc(Long userId);
    List<DailySymptom> findByUserIdOrderByDateDesc(Long userId);

}
