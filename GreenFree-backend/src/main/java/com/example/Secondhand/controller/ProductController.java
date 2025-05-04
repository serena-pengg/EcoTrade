package com.example.Secondhand.controller;

import com.example.Secondhand.dto.ProductFilter;
import com.example.Secondhand.model.Product;
import com.example.Secondhand.model.Order;
import com.example.Secondhand.service.ProductService;
import com.example.Secondhand.service.RecommendationService;
import com.example.Secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5175", "http://localhost:8080"}, 
             allowedHeaders = "*",
             allowCredentials = "true",
             maxAge = 3600)
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String ecoScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "date") String sort
    ) {
        try {
            ProductFilter filter = new ProductFilter();
            filter.setQuery(query);
            filter.setCategory(category);
            filter.setPriceRange(priceRange);
            filter.setEcoScore(ecoScore);
            filter.setSort(sort);

            List<Product> allProducts = productService.getFilteredProducts(filter);
            
            // 计算分页
            int total = allProducts.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, total);
            
            // 防止索引越界
            if (startIndex >= total) {
                startIndex = 0;
                endIndex = Math.min(size, total);
            }
            
            List<Product> paginatedProducts = allProducts.subList(startIndex, endIndex);

            Map<String, Object> response = new HashMap<>();
            response.put("products", paginatedProducts);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch products: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product not found: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/products/{id}/recommendations")
    public ResponseEntity<List<Product>> getProductRecommendations(@PathVariable Long id) {
        try {
        Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }

            List<Set<Long>> transactionList = getTransactionHistory();
            List<Product> recommendedProducts = recommendationService.getEcoWeightedRecommendations(
                transactionList, product);
            
            return ResponseEntity.ok(recommendedProducts);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/cart/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId) {
        try {
            // 这里添加购物车逻辑
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product added to cart successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to add to cart: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/products/{id}")
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

    @GetMapping("/products/recommended")
    public ResponseEntity<?> getRecommendedProducts() {
        try {
            // 获取所有交易记录
            List<Set<Long>> transactionList = getTransactionHistory();
            
            // 获取所有商品
            List<Product> allProducts = productService.getAllProducts();
            
            if (!allProducts.isEmpty()) {
                // 选择一个随机商品作为基准商品
                Product randomProduct = allProducts.get(new Random().nextInt(allProducts.size()));
            
            // 获取推荐商品
                List<Product> recommendedProducts = new ArrayList<>(
                    recommendationService.getEcoWeightedRecommendations(transactionList, randomProduct)
                );
                
                // 如果推荐商品不足8个，用环保评分高的商品补充
                if (recommendedProducts.size() < 8) {
                    List<Product> ecoProducts = allProducts.stream()
                        .filter(p -> !recommendedProducts.contains(p))
                        .sorted((p1, p2) -> p2.getEcoScore().compareTo(p1.getEcoScore()))
                        .limit(8 - recommendedProducts.size())
                        .collect(Collectors.toList());
                    recommendedProducts.addAll(ecoProducts);
                }
                
                return ResponseEntity.ok(recommendedProducts);
            }
            
            // 如果没有商品，返回空列表
            return ResponseEntity.ok(Collections.emptyList());
            
        } catch (Exception e) {
            // 如果出错，返回最新的8个商品
            List<Product> latestProducts = productService.getLatestProducts(8);
            return ResponseEntity.ok(latestProducts);
        }
    }

    private List<Set<Long>> getTransactionHistory() {
        List<Order> allOrders = orderService.getAllOrders();
        return allOrders.stream()
            .map(order -> order.getOrderItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet()))
            .collect(Collectors.toList());
    }
} 