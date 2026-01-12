package com.example.pcos.health.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_symptom",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"})
)
public class DailySymptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private Integer cramps;
    private Integer acne;
    private Integer mood;
    private Integer bloating;
    private Integer fatigue;
    private Integer headache;

    // 🌸 AI-generated wellness insight for dashboard
    @Column(columnDefinition = "TEXT")
    private String wellnessMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --------------------
    // Getters & Setters
    // --------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getCramps() {
        return cramps;
    }

    public void setCramps(Integer cramps) {
        this.cramps = cramps;
    }

    public Integer getAcne() {
        return acne;
    }

    public void setAcne(Integer acne) {
        this.acne = acne;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }

    public Integer getBloating() {
        return bloating;
    }

    public void setBloating(Integer bloating) {
        this.bloating = bloating;
    }

    public Integer getFatigue() {
        return fatigue;
    }

    public void setFatigue(Integer fatigue) {
        this.fatigue = fatigue;
    }

    public Integer getHeadache() {
        return headache;
    }

    public void setHeadache(Integer headache) {
        this.headache = headache;
    }

    public String getWellnessMessage() {
        return wellnessMessage;
    }

    public void setWellnessMessage(String wellnessMessage) {
        this.wellnessMessage = wellnessMessage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}