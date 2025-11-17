package com.example.pcos.health.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "daily_symptom")
public class DailySymptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    private LocalDate date;

    // Slider values: 0 - 10
    @Min(value = 0, message = "Value must be between 0 and 10")
    @Max(value = 10, message = "Value must be between 0 and 10")
    private int cramps = 0;

    @Min(value = 0, message = "Value must be between 0 and 10")
    @Max(value = 10, message = "Value must be between 0 and 10")
    private int acne = 0;

    @Min(value = 0, message = "Value must be between 0 and 10")
    @Max(value = 10, message = "Value must be between 0 and 10")
    private int mood = 0;

    @Min(value = 0, message = "Value must be between 0 and 10")
    @Max(value = 10, message = "Value must be between 0 and 10")
    private int bloating = 0;

    @Min(value = 0, message = "Value must be between 0 and 10")
    @Max(value = 10, message = "Value must be between 0 and 10")
    private int fatigue = 0;

    @Min(value = 0, message = "Value must be between 0 and 10")
    @Max(value = 10, message = "Value must be between 0 and 10")
    private int headache = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public DailySymptom() {
        this.date = LocalDate.now();
    }

    // Optional convenience constructor
    public DailySymptom(LocalDate date, int cramps, int acne, int mood, int bloating, int fatigue, int headache, User user) {
        this.date = date;
        this.cramps = clamp(cramps);
        this.acne = clamp(acne);
        this.mood = clamp(mood);
        this.bloating = clamp(bloating);
        this.fatigue = clamp(fatigue);
        this.headache = clamp(headache);
        this.user = user;
    }

    // --- getters & setters ---

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

    public int getCramps() {
        return cramps;
    }

    public void setCramps(int cramps) {
        this.cramps = clamp(cramps);
    }

    public int getAcne() {
        return acne;
    }

    public void setAcne(int acne) {
        this.acne = clamp(acne);
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = clamp(mood);
    }

    public int getBloating() {
        return bloating;
    }

    public void setBloating(int bloating) {
        this.bloating = clamp(bloating);
    }

    public int getFatigue() {
        return fatigue;
    }

    public void setFatigue(int fatigue) {
        this.fatigue = clamp(fatigue);
    }

    public int getHeadache() {
        return headache;
    }

    public void setHeadache(int headache) {
        this.headache = clamp(headache);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Utility to ensure values stay within 0-10
    private int clamp(int v) {
        if (v < 0) return 0;
        if (v > 10) return 10;
        return v;
    }
}
