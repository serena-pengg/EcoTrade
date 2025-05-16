package com.example.Secondhand.controller;

import com.example.Secondhand.dto.UserDTO;
import com.example.Secondhand.model.Product;
import com.example.Secondhand.model.User;
import com.example.Secondhand.model.Order;
import com.example.Secondhand.service.ProductService;
import com.example.Secondhand.service.UserService;
import com.example.Secondhand.service.OrderService;
import com.example.Secondhand.dto.UserUpdateDTO;
import com.example.Secondhand.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(UserDTO userDTO, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(userDTO);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            User user = userService.getCurrentUser(authentication);
            model.addAttribute("user", user);
        }

        try {
            // 获取所有交易记录
            List<Set<Long>> transactionList = getTransactionHistory();
            
            // 获取所有商品
            List<Product> allProducts = productService.getAllProducts();
            
            if (!allProducts.isEmpty()) {
                // 选择一个随机商品作为基准商品（可以根据需求修改选择策略）
                Product randomProduct = allProducts.get(new Random().nextInt(allProducts.size()));
                
                // 获取推荐商品
                List<Product> recommendedProducts = new ArrayList<>(
                    recommendationService.getEcoWeightedRecommendations(transactionList, randomProduct)
                );
                
                // 如果推荐商品不足8个，先用环保评分高的商品补充
                if (recommendedProducts.size() < 8) {
                    // 获取环保评分高的商品（排除已推荐的商品）
                    List<Product> ecoFriendlyProducts = allProducts.stream()
                        .filter(p -> !recommendedProducts.contains(p))
                        .filter(p -> p.getEcoScore() != null)
                        .sorted((p1, p2) -> Float.compare(
                            p2.getEcoScore() != null ? p2.getEcoScore() : 0,
                            p1.getEcoScore() != null ? p1.getEcoScore() : 0))
                        .limit(8 - recommendedProducts.size())
                        .collect(Collectors.toList());
                    recommendedProducts.addAll(ecoFriendlyProducts);
                    
                    // 如果还不足8个，用最新商品补充
                    if (recommendedProducts.size() < 8) {
                        List<Product> latestProducts = allProducts.stream()
                            .filter(p -> !recommendedProducts.contains(p))
                            .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                            .limit(8 - recommendedProducts.size())
                            .collect(Collectors.toList());
                        recommendedProducts.addAll(latestProducts);
                    }
                }
                
                // 限制只显示8个商品
                final List<Product> finalRecommendations = recommendedProducts.stream()
                    .limit(8)
                    .collect(Collectors.toList());
                
                model.addAttribute("recommendedProducts", finalRecommendations);
            } else {
                model.addAttribute("recommendedProducts", Collections.emptyList());
            }
            
        } catch (Exception e) {
            // 如果推荐系统出错，显示最新商品
            List<Product> latestProducts = productService.getAllProducts().stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(8)
                .collect(Collectors.toList());
            model.addAttribute("recommendedProducts", latestProducts);
        }

        return "Home";
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

    @GetMapping("/user/profile")
    public String profile(Model model, Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Get user's orders
        List<Order> userOrders = orderService.getOrdersByUserId(currentUser.getId());
        
        userService.recalculateEcoPoints(currentUser);
        User refreshedUser = userService.getUserById(currentUser.getId());
        model.addAttribute("user", refreshedUser);
        model.addAttribute("orders", userOrders);
        
        return "profile";
    }

    @GetMapping("/contact")
    public String contact(Model model, Authentication authentication) {
        if (authentication != null) {
            User user = userService.getCurrentUser(authentication);
            model.addAttribute("user", user);
        }
        return "contact";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String phoneNumber,
                              @RequestParam(required = false) String address,
                              @RequestParam(required = false) String bio,
                              @RequestParam(required = false) String currentPassword,
                              @RequestParam(required = false) String newPassword,
                              @RequestParam(required = false) String confirmNewPassword,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getCurrentUser(authentication);
            if (currentUser == null) {
                return "redirect:/login";
            }

            UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
            userUpdateDTO.setUsername(username);
            userUpdateDTO.setPhoneNumber(phoneNumber);
            userUpdateDTO.setAddress(address);
            userUpdateDTO.setBio(bio);
            userUpdateDTO.setCurrentPassword(currentPassword);
            userUpdateDTO.setNewPassword(newPassword);
            userUpdateDTO.setConfirmNewPassword(confirmNewPassword);

            userService.updateUserProfile(currentUser.getId(), userUpdateDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/user/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/profile";
        }
    }

    // @GetMapping("/cart")
    // public String cart() {
    //     return "cart";
    // }

    @PostMapping("/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            User updatedUser = userService.updateUserProfile(userId, userUpdateDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/refresh-eco-points")
    @ResponseBody
    public String refreshEcoPoints() {
        userService.recalculateAllUsersEcoPoints();
        return "Eco points refreshed!";
    }

    @GetMapping("/api/recommended-products")
    @ResponseBody
    public List<Product> getRecommendedProducts(
            @RequestParam(required = false) String sort,
            Authentication authentication) {
        List<Product> recommendedProducts = getRecommendedProductsList(authentication);
        
        // 根据排序参数对商品进行排序
        if (sort != null) {
            switch (sort) {
                case "eco-score":
                    recommendedProducts.sort((p1, p2) -> Float.compare(
                        p2.getEcoScore() != null ? p2.getEcoScore() : 0,
                        p1.getEcoScore() != null ? p1.getEcoScore() : 0));
                    break;
                case "price-asc":
                    recommendedProducts.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "date":
                    recommendedProducts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                    break;
            }
        }
        
        return recommendedProducts;
    }

    private List<Product> getRecommendedProductsList(Authentication authentication) {
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
            
            // 如果推荐商品不足8个，补充商品
            if (recommendedProducts.size() < 8) {
                // 获取环保评分高的商品
                List<Product> ecoFriendlyProducts = allProducts.stream()
                    .filter(p -> !recommendedProducts.contains(p))
                    .filter(p -> p.getEcoScore() != null)
                    .sorted((p1, p2) -> Float.compare(
                        p2.getEcoScore() != null ? p2.getEcoScore() : 0,
                        p1.getEcoScore() != null ? p1.getEcoScore() : 0))
                    .limit(8 - recommendedProducts.size())
                    .collect(Collectors.toList());
                recommendedProducts.addAll(ecoFriendlyProducts);
                
                // 如果还不足8个，用最新商品补充
                if (recommendedProducts.size() < 8) {
                    List<Product> latestProducts = allProducts.stream()
                        .filter(p -> !recommendedProducts.contains(p))
                        .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                        .limit(8 - recommendedProducts.size())
                        .collect(Collectors.toList());
                    recommendedProducts.addAll(latestProducts);
                }
            }
            
            return recommendedProducts.stream()
                .limit(8)
                .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
} 