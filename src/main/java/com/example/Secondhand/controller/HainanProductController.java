package com.example.Secondhand.controller;

import com.example.Secondhand.model.HainanProduct;
import com.example.Secondhand.service.HainanProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hainan-products")
public class HainanProductController {

    @Autowired
    private HainanProductService hainanProductService;

    @GetMapping
    public List<HainanProduct> getAllProducts() {
        return hainanProductService.getAllProducts();
    }

    @GetMapping("/{id}")
    public HainanProduct getProductById(@PathVariable Long id) {
        return hainanProductService.getProductById(id);
    }

    @PostMapping
    public HainanProduct createProduct(@RequestBody HainanProduct product) {
        return hainanProductService.createProduct(product);
    }

    @PutMapping("/{id}")
    public HainanProduct updateProduct(@PathVariable Long id, @RequestBody HainanProduct product) {
        return hainanProductService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        hainanProductService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/category/{category}")
    public List<HainanProduct> getProductsByCategory(@PathVariable String category) {
        return hainanProductService.getProductsByCategory(category);
    }

    @GetMapping("/filtered")
    public Page<HainanProduct> getFilteredProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "ecoScore", required = false) String ecoScore,
            @RequestParam(value = "priceRange", required = false) String priceRange) {
        int pageSize = 3;
        return hainanProductService.getFilteredProducts(
            category, ecoScore, priceRange, PageRequest.of(page, pageSize, Sort.by("createdAt").descending())
        );
    }

}