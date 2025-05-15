package com.example.Secondhand.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "hainan_products")
public class HainanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "category")
    private String category;

    @Column(name = "recycle_score")
    private Float recycleScore;

    @Column(name = "durability_score")
    private Float durabilityScore;

    @Column(name = "carbon_footprint")
    private Float carbonFootprint;

    @Column(name = "eco_score")
    private Float ecoScore;

    private static final float EPSILON = 1e-6f;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateEcoScore();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateEcoScore();
    }

    @PostLoad
    protected void onLoad() {
        calculateEcoScore();
    }

    private void calculateEcoScore() {
        if (recycleScore != null && durabilityScore != null && carbonFootprint != null) {
            float recycleWeight = 0.4f;
            float durabilityWeight = 0.3f;
            float carbonWeight = 0.3f;

            float normalizedRecycleScore = Math.min(Math.max(recycleScore, 1f), 5f);
            float normalizedDurabilityScore = Math.min(Math.max(durabilityScore, 1f), 5f);
            float normalizedCarbonScore = Math.min(Math.max(carbonFootprint, 1f), 5f);

            ecoScore = (normalizedRecycleScore * recycleWeight) +
                      (normalizedDurabilityScore * durabilityWeight) +
                      (normalizedCarbonScore * carbonWeight);
            ecoScore = Math.min(Math.max(ecoScore, 1f), 5f);
        } else {
            ecoScore = null;
        }
    }

    public void setRecycleScore(Float recycleScore) {
        this.recycleScore = recycleScore;
        calculateEcoScore();
    }

    public void setDurabilityScore(Float durabilityScore) {
        this.durabilityScore = durabilityScore;
        calculateEcoScore();
    }

    public void setCarbonFootprint(Float carbonFootprint) {
        this.carbonFootprint = carbonFootprint;
        calculateEcoScore();
    }
} 