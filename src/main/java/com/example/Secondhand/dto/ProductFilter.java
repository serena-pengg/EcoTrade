package com.example.Secondhand.dto;

import lombok.Data;

@Data
public class ProductFilter {
    private String query;
    private String category;
    private String priceRange;
    private String dateFilter;
    private String area;
    private String sort;
    private String ecoScore;
} 