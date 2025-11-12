package com.example.pcos.health.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.pcos.health.tracker.entity.Symptom;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {
}
