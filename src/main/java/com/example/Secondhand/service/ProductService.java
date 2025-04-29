package com.example.Secondhand.service;

import com.example.Secondhand.dto.ProductFilter;
import com.example.Secondhand.model.Product;
import com.example.Secondhand.model.User;
import com.example.Secondhand.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    public List<Product> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public List<Product> searchProducts(String query, int page, int size) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            query, query, PageRequest.of(page, size)).getContent();
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public List<Product> getFilteredProducts(ProductFilter filter) {
        List<Product> products = getAllProducts();

        // 应用搜索过滤
        if (filter.getQuery() != null && !filter.getQuery().trim().isEmpty()) {
            String query = filter.getQuery().toLowerCase();
            products = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(query) || 
                           (p.getDescription() != null && p.getDescription().toLowerCase().contains(query)))
                .collect(Collectors.toList());
        }

        // 应用分类过滤
        if (filter.getCategory() != null && !filter.getCategory().equals("All")) {
            products = products.stream()
                .filter(p -> p.getCategory().equals(filter.getCategory()))
                .collect(Collectors.toList());
        }

        // 应用价格过滤
        if (filter.getPriceRange() != null && !filter.getPriceRange().equals("all")) {
            switch (filter.getPriceRange()) {
                case "0-500":
                    products = products.stream()
                        .filter(p -> p.getPrice() >= 0 && p.getPrice() <= 500)
                        .collect(Collectors.toList());
                    break;
                case "500-1000":
                    products = products.stream()
                        .filter(p -> p.getPrice() > 500 && p.getPrice() <= 1000)
                        .collect(Collectors.toList());
                    break;
                case "1000-2000":
                    products = products.stream()
                        .filter(p -> p.getPrice() > 1000 && p.getPrice() <= 2000)
                        .collect(Collectors.toList());
                    break;
                case "2000+":
                    products = products.stream()
                        .filter(p -> p.getPrice() > 2000)
                        .collect(Collectors.toList());
                    break;
            }
        }

        // 应用环保分数过滤
        if (filter.getEcoScore() != null && !filter.getEcoScore().equals("all")) {
            switch (filter.getEcoScore()) {
                case "excellent":
                    products = products.stream()
                        .filter(p -> p.getEcoScore() != null && p.getEcoScore() >= 4.0)
                        .collect(Collectors.toList());
                    break;
                case "good":
                    products = products.stream()
                        .filter(p -> p.getEcoScore() != null && p.getEcoScore() >= 3.0 && p.getEcoScore() < 4.0)
                        .collect(Collectors.toList());
                    break;
                case "fair":
                    products = products.stream()
                        .filter(p -> p.getEcoScore() != null && p.getEcoScore() >= 2.0 && p.getEcoScore() < 3.0)
                        .collect(Collectors.toList());
                    break;
                case "poor":
                    products = products.stream()
                        .filter(p -> p.getEcoScore() != null && p.getEcoScore() < 2.0)
                        .collect(Collectors.toList());
                    break;
            }
        }

        // 应用日期过滤
        if (filter.getDateFilter() != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (filter.getDateFilter()) {
                case "lastWeek":
                    products = products.stream()
                        .filter(p -> p.getCreatedAt().isAfter(now.minusWeeks(1)))
                        .collect(Collectors.toList());
                    break;
                case "lastMonth":
                    products = products.stream()
                        .filter(p -> p.getCreatedAt().isAfter(now.minusMonths(1)))
                        .collect(Collectors.toList());
                    break;
                case "thisYear":
                    products = products.stream()
                        .filter(p -> p.getCreatedAt().isAfter(now.withDayOfYear(1)))
                        .collect(Collectors.toList());
                    break;
            }
        }

        // 应用排序
        if (filter.getSort() != null) {
            switch (filter.getSort()) {
                case "date":
                    products.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                    break;
                case "price-asc":
                    products.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "price-desc":
                    products.sort((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()));
                    break;
            }
        }

        return products;
    }

    public List<Product> getLatestProducts(int limit) {
        return productRepository.findLatestProducts(PageRequest.of(0, limit));
    }
} 