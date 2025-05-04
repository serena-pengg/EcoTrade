package com.example.Secondhand.controller;

import com.example.Secondhand.model.*;
import com.example.Secondhand.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ShippingAddressService shippingAddressService;

    @GetMapping("/checkout")
    public String showCheckout(Model model, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(currentUser);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cartService.calculateTotal(currentUser));
        
        return "payment-info";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam String fullName,
                         @RequestParam String phoneNumber,
                         @RequestParam String address,
                         @RequestParam(required = false) String deliveryNotes,
                         @RequestParam(required = false, defaultValue = "false") boolean saveAddress) {
        
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (saveAddress) {
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setFullName(fullName);
            shippingAddress.setPhoneNumber(phoneNumber);
            shippingAddress.setAddress(address);
            shippingAddress.setDefault(true);
            shippingAddress.setUser(currentUser);
            shippingAddressService.saveAddress(shippingAddress);
        }

        orderService.createOrder(currentUser, fullName, phoneNumber, address, deliveryNotes);
        cartService.clearCart(currentUser);
        
        return "redirect:/order/success";
    }

    @GetMapping("/success")
    public String orderSuccess() {
        return "order-success";
    }
} 