package com.example.pcos.health.tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String category;

    @Column(name = "pcosTag")
    private String pcosTag;


    private String description;

    // Constructors
    public Food() {}

    public Food(String name, String category, String pcosTag, String description) {
        this.name = name;
        this.category = category;
        this.pcosTag = pcosTag;
        this.description = description;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPcosTag() { return pcosTag; }
    public void setPcosTag(String pcosTag) { this.pcosTag = pcosTag; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
