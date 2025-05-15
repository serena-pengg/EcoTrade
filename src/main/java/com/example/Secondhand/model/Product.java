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

    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "seller_name")
    private String sellerName;

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

    @Column(name = "min_carbon_footprint")
    private Float minCarbonFootprint;

    @Column(name = "max_carbon_footprint")
    private Float maxCarbonFootprint;

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
            // 权重设置
            float recycleWeight = 0.4f;    // 可回收性权重
            float durabilityWeight = 0.3f;  // 使用寿命权重
            float carbonWeight = 0.3f;      // 碳足迹权重

            // 确保评分在1-5范围内
            float normalizedRecycleScore = Math.min(Math.max(recycleScore, 1f), 5f);
            float normalizedDurabilityScore = Math.min(Math.max(durabilityScore, 1f), 5f);

            // 碳足迹标准归一化 + 反向转化
            // carbon_score = 1 - (x - min(x)) / (max(x) - min(x) + ε)
            float minCarbon = minCarbonFootprint != null ? minCarbonFootprint : 0f;
            float maxCarbon = maxCarbonFootprint != null ? maxCarbonFootprint : 100f;
            
            float normalizedCarbonScore = 1f - (carbonFootprint - minCarbon) / (maxCarbon - minCarbon + EPSILON);
            // 将 [0,1] 范围转换为 [1,5] 范围
            normalizedCarbonScore = normalizedCarbonScore * 4f + 1f;
            // 确保在1-5范围内
            normalizedCarbonScore = Math.min(Math.max(normalizedCarbonScore, 1f), 5f);

            // 计算综合环保评分
            ecoScore = (normalizedRecycleScore * recycleWeight) + 
                      (normalizedDurabilityScore * durabilityWeight) + 
                      (normalizedCarbonScore * carbonWeight);
            
            // 确保 eco score 在1-5范围内
            ecoScore = Math.min(Math.max(ecoScore, 1f), 5f);
        } else {
            // 如果缺少任何评分，将 eco score 设置为 null
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