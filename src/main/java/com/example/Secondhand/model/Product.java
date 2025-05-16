package com.example.Secondhand.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    private float calculateCarbonScore(float carbonFootprint) {
        if (carbonFootprint <= 0) {
            return 5.0f; // 零碳足迹或负碳足迹（如碳汇）给予最高分
        } else if (carbonFootprint <= 2) {
            return 5.0f; // 极低碳足迹
        } else if (carbonFootprint <= 5) {
            return 4.0f; // 低碳足迹
        } else if (carbonFootprint <= 10) {
            return 3.0f; // 中等碳足迹
        } else if (carbonFootprint <= 20) {
            return 2.0f; // 较高碳足迹
        } else {
            return 1.0f; // 高碳足迹
        }
    }

    private void calculateEcoScore() {
        if (recycleScore != null && durabilityScore != null && carbonFootprint != null) {
            // 权重设置
            float recycleWeight = 0.4f;    // 可回收性权重
            float durabilityWeight = 0.3f;  // 使用寿命权重
            float carbonWeight = 0.3f;      // 碳足迹权重

            // 确保评分在1-5范围内
            float normalizedRecycleScore = Math.min(Math.max(recycleScore, 1f), 5f);
            float normalizedDurabilityScore = Math.min(Math.max(durabilityScore, 1f), 5f);
            
            // 计算碳足迹评分
            float normalizedCarbonScore = calculateCarbonScore(carbonFootprint);

            // 计算综合环保评分
            ecoScore = (normalizedRecycleScore * recycleWeight) + 
                      (normalizedDurabilityScore * durabilityWeight) + 
                      (normalizedCarbonScore * carbonWeight);
            
            // 确保 eco score 在1-5范围内
            ecoScore = Math.min(Math.max(ecoScore, 1f), 5f);
        } else {
            ecoScore = null;
        }
    }

    // 重写 setter 方法以确保每次设置评分时都重新计算 eco score
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