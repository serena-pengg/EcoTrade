package com.example.Secondhand.controller;

import com.example.Secondhand.model.HainanProduct;
import com.example.Secondhand.service.HainanProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HainanProductPageController {
    @Autowired
    private HainanProductService hainanProductService;

    @GetMapping("/HainanProduct")
    public String hainanProductPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "ecoScore", required = false) String ecoScore,
            @RequestParam(value = "priceRange", required = false) String priceRange,
            Model model) {
        int pageSize = 3;
        Page<HainanProduct> productPage = hainanProductService.getFilteredProducts(
            category, ecoScore, priceRange, PageRequest.of(page, pageSize, Sort.by("createdAt").descending())
        );
        model.addAttribute("searchResults", productPage.getContent());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedEcoScore", ecoScore);
        model.addAttribute("selectedPriceRange", priceRange);
        return "HainanProduct"; // 视图名，自动找 HainanProduct.html
    }
}