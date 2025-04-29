package com.example.Secondhand.controller;

import com.example.Secondhand.model.ShippingAddress;
import com.example.Secondhand.model.User;
import com.example.Secondhand.service.ShippingAddressService;
import com.example.Secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/address")
public class ShippingAddressController {

    @Autowired
    private ShippingAddressService shippingAddressService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String getAddresses(Model model) {
        User currentUser = getCurrentUser();
        List<ShippingAddress> addresses = shippingAddressService.getUserAddresses(currentUser);
        model.addAttribute("addresses", addresses);
        return "address/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("address", new ShippingAddress());
        return "address/form";
    }

    @PostMapping("/add")
    public String addAddress(@ModelAttribute ShippingAddress address) {
        User currentUser = getCurrentUser();
        address.setUser(currentUser);
        shippingAddressService.saveAddress(address);
        return "redirect:/address";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        List<ShippingAddress> addresses = shippingAddressService.getUserAddresses(currentUser);
        ShippingAddress address = addresses.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid address id: " + id));
        model.addAttribute("address", address);
        return "address/form";
    }

    @PostMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody ShippingAddress address) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(new ApiResponse(false, "用户未登录"));
            }
            
            List<ShippingAddress> userAddresses = shippingAddressService.getUserAddresses(currentUser);
            boolean addressBelongsToUser = userAddresses.stream()
                    .anyMatch(addr -> addr.getId().equals(id));
                    
            if (!addressBelongsToUser) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "没有权限更新此地址"));
            }
            
            // 设置地址ID和用户
            address.setId(id);
            address.setUser(currentUser);
            
            // 保存更新后的地址
            shippingAddressService.saveAddress(address);
            
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new ApiResponse(true, "地址更新成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new ApiResponse(false, "更新地址失败: " + e.getMessage()));
        }
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(new ApiResponse(false, "User not logged in"));
            }
            
            List<ShippingAddress> userAddresses = shippingAddressService.getUserAddresses(currentUser);
            
            // 验证地址是否属于当前用户
            boolean addressBelongsToUser = userAddresses.stream()
                    .anyMatch(address -> address.getId().equals(id));
                    
            if (addressBelongsToUser) {
                try {
                    shippingAddressService.deleteAddress(id);
                    return ResponseEntity.ok()
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .body(new ApiResponse(true, "Address deleted successfully"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.badRequest()
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .body(new ApiResponse(false, "Failed to delete address: " + e.getMessage()));
                }
            }
            
            return ResponseEntity.badRequest()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new ApiResponse(false, "No permission to delete this address"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new ApiResponse(false, "Server error: " + e.getMessage()));
        }
    }

    @PostMapping("/set-default/{id}")
    @ResponseBody
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(new ApiResponse(false, "User not logged in"));
            }
            
            List<ShippingAddress> addresses = shippingAddressService.getUserAddresses(currentUser);
            boolean addressExists = addresses.stream().anyMatch(addr -> addr.getId().equals(id));
            
            if (!addressExists) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Address not found"));
            }
            
            addresses.forEach(address -> {
                address.setDefault(address.getId().equals(id));
                shippingAddressService.saveAddress(address);
            });
            
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new ApiResponse(true, "Default address set successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new ApiResponse(false, "Failed to set default address: " + e.getMessage()));
        }
    }

    @PostMapping("/save")
    public String saveAddress(@RequestParam String fullName,
                            @RequestParam String phoneNumber,
                            @RequestParam String address,
                            @RequestParam(required = false) boolean saveAddress) {
        User currentUser = getCurrentUser();
        
        if (saveAddress) {
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setUser(currentUser);
            shippingAddress.setFullName(fullName);
            shippingAddress.setPhoneNumber(phoneNumber);
            shippingAddress.setAddress(address);
            shippingAddress.setDefault(true);
            
            shippingAddressService.saveAddress(shippingAddress);
        }
        
        return "redirect:/cart/order-success";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userService.getCurrentUser(authentication);
    }

    // 添加一个简单的响应类
    private static class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
} 