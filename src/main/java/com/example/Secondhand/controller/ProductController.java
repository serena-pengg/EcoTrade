package com.example.Secondhand.controller;

import com.example.Secondhand.dto.ProductFilter;
import com.example.Secondhand.model.Product;
import com.example.Secondhand.model.Order;
import com.example.Secondhand.service.ProductService;
import com.example.Secondhand.service.RecommendationService;
import com.example.Secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/products")
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/products/category/{category}")
    public String getProductsByCategory(@PathVariable String category, Model model) {
        List<Product> products = productService.getProductsByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        return "products";
    }

    @GetMapping("/products/seller/{sellerId}")
    public String getProductsBySeller(@PathVariable Long sellerId, Model model) {
        List<Product> products = productService.getProductsBySeller(sellerId);
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/product/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-detail";
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            productService.deleteProduct(id);
            response.put("message", "Product deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping({"/search", "/search-results"})
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String ecoScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            Model model) {
        
        // 构建过滤条件
        ProductFilter filter = new ProductFilter();
        filter.setQuery(query);
        filter.setCategory(category);
        filter.setPriceRange(priceRange);
        filter.setDateFilter(dateFilter);
        filter.setArea(area);
        filter.setSort(sort);
        filter.setEcoScore(ecoScore);
        
        List<Product> products = productService.getFilteredProducts(filter);
        
        // 计算总页数
        int totalItems = products.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // 获取当前页的商品
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        List<Product> pageProducts = products.subList(startIndex, endIndex);
        
        model.addAttribute("searchResults", pageProducts);
        model.addAttribute("query", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedDateFilter", dateFilter);
        model.addAttribute("selectedArea", area);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedEcoScore", ecoScore);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
        return "search-results";
    }

    /**
     * 获取商品详情页面，包含推荐商品
     */
    @GetMapping("/item/{id}")
    public String getProductDetails(@PathVariable Long id, Model model) {
        // 获取商品详情
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/search-results";
        }
        model.addAttribute("product", product);

        try {
            // 获取用户的购买历史记录
            List<Set<Long>> transactionList = getTransactionHistory();
            
            // 获取推荐商品
            List<Product> recommendedProducts = recommendationService.getEcoWeightedRecommendations(
                transactionList, product);
            
            model.addAttribute("recommendedProducts", recommendedProducts);
        } catch (Exception e) {
            // 如果推荐系统出错，不影响商品详情页的显示
            model.addAttribute("recommendedProducts", Collections.emptyList());
        }
        
        return "product-details";
    }

    /**
     * 获取用户的购买历史记录
     */
    private List<Set<Long>> getTransactionHistory() {
        // 从订单服务获取所有订单
        List<Order> allOrders = orderService.getAllOrders();
        
        // 将订单转换为交易记录列表
        return allOrders.stream()
            .map(order -> order.getOrderItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet()))
            .collect(Collectors.toList());
    }
} 