package com.example.pcos.health.tracker.repository;

import com.example.pcos.health.tracker.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
    List<MedicalReport> findByUserId(Long userId);
}
