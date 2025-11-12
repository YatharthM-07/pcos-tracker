package com.example.pcos.health.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symptomName;
    private String severity; // mild, moderate, severe
    private LocalDate dateRecorded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Symptom() {}

    public Symptom(String symptomName, String severity, LocalDate dateRecorded, User user) {
        this.symptomName = symptomName;
        this.severity = severity;
        this.dateRecorded = dateRecorded;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymptomName() { return symptomName; }
    public void setSymptomName(String symptomName) { this.symptomName = symptomName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDate getDateRecorded() { return dateRecorded; }
    public void setDateRecorded(LocalDate dateRecorded) { this.dateRecorded = dateRecorded; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
