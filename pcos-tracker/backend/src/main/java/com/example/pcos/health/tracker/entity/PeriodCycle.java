package com.example.pcos.health.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "period_cycles")
public class PeriodCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Relationship
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ⭐ Period start
    @Setter
    @Column(nullable = false)
    private LocalDate startDate;

    // ⭐ Period end
    @Setter
    @Column(nullable = false)
    private LocalDate endDate;

    // ---------- GETTERS & SETTERS ----------

}
