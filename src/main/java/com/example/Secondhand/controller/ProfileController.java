package com.example.Secondhand.controller;

import com.example.Secondhand.model.User;
import com.example.Secondhand.model.ShippingAddress;
import com.example.Secondhand.model.Order;
import com.example.Secondhand.dto.UserUpdateDTO;
import com.example.Secondhand.service.UserService;
import com.example.Secondhand.service.ShippingAddressService;
import com.example.Secondhand.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ShippingAddressService shippingAddressService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        // 获取用户保存的地址
        List<ShippingAddress> savedAddresses = shippingAddressService.getUserAddresses(user);
        
        // 获取用户的订单历史
        List<Order> orders = orderService.getUserOrders(user);

        model.addAttribute("user", user);
        model.addAttribute("savedAddresses", savedAddresses);
        model.addAttribute("orders", orders);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserUpdateDTO userUpdateDTO,
                              Authentication authentication) {
        
        User user = userService.getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            userService.updateUserProfile(user.getId(), userUpdateDTO);
            return "redirect:/profile?success=Profile updated successfully";
        } catch (Exception e) {
            return "redirect:/profile?error=" + e.getMessage();
        }
    }
} 