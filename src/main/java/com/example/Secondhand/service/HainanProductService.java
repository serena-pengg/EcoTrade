package com.example.Secondhand.service;

import com.example.Secondhand.model.HainanProduct;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HainanProductService {
    List<HainanProduct> getAllProducts();
    HainanProduct getProductById(Long id);
    HainanProduct createProduct(HainanProduct product);
    HainanProduct updateProduct(Long id, HainanProduct product);
    void deleteProduct(Long id);
    List<HainanProduct> getProductsByCategory(String category);
    Page<HainanProduct> getProductsPaged(Pageable pageable);
    Page<HainanProduct> getFilteredProducts(String category, String ecoScore, String priceRange, Pageable pageable);
} 
 