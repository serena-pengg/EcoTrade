package com.example.Secondhand.service.impl;

import com.example.Secondhand.dto.UserDTO;
import com.example.Secondhand.dto.UserUpdateDTO;
import com.example.Secondhand.model.User;
import com.example.Secondhand.model.Order;
import com.example.Secondhand.model.OrderItem;
import com.example.Secondhand.model.Product;
import com.example.Secondhand.repository.UserRepository;
import com.example.Secondhand.service.UserService;
import com.example.Secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Lazy
    private OrderService orderService;

    @Override
    public User registerUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setUsername(userDTO.getUsername());
        return userRepository.save(user);
    }

    @Override
    public Authentication login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return getUserByEmail(authentication.getName());
        }
        return null;
    }

    @Override
    public User updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic information
        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().isEmpty()) {
            user.setUsername(userUpdateDTO.getUsername());
        }
        if (userUpdateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userUpdateDTO.getPhoneNumber());
        }
        if (userUpdateDTO.getAddress() != null) {
            user.setAddress(userUpdateDTO.getAddress());
        }
        if (userUpdateDTO.getBio() != null) {
            user.setBio(userUpdateDTO.getBio());
        }

        // Update password if provided
        if (userUpdateDTO.getCurrentPassword() != null && 
            userUpdateDTO.getNewPassword() != null && 
            userUpdateDTO.getConfirmNewPassword() != null) {
            
            // Verify current password
            if (!passwordEncoder.matches(userUpdateDTO.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            // Verify new password confirmation
            if (!userUpdateDTO.getNewPassword().equals(userUpdateDTO.getConfirmNewPassword())) {
                throw new RuntimeException("New passwords do not match");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(userUpdateDTO.getNewPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void recalculateEcoPoints(User user) {
        System.out.println("User: " + user.getId() + ", email: " + user.getEmail());
        List<Order> userOrders = orderService.getOrdersByUserId(user.getId());
        System.out.println("Orders found: " + userOrders.size());
        int totalEcoPoints = 0;
        for (Order order : userOrders) {
            System.out.println("Order: " + order.getId() + ", status: " + order.getStatus());
            for (OrderItem orderItem : order.getOrderItems()) {
                Product product = orderItem.getProduct();
                System.out.println("OrderItem: " + orderItem.getId() + ", Product: " + (product != null ? product.getId() : "null") + ", EcoScore: " + (product != null ? product.getEcoScore() : "null"));
                if (product != null && product.getEcoScore() != null) {
                    int basePoints = (int)(product.getEcoScore() * 50);
                    int quantityBonus = orderItem.getQuantity() * 10;
                    int itemEcoPoints = basePoints + quantityBonus;
                    totalEcoPoints += itemEcoPoints;
                }
            }
        }
        System.out.println("Total eco points: " + totalEcoPoints);
        user.setEcoPoints(totalEcoPoints);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void recalculateAllUsersEcoPoints() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            recalculateEcoPoints(user);
        }
    }

    // 每天凌晨2点自动刷新
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledEcoPointsRefresh() {
        recalculateAllUsersEcoPoints();
    }
} 