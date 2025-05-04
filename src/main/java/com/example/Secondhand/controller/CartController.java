package com.example.Secondhand.controller;

import com.example.Secondhand.model.CartItem;
import com.example.Secondhand.model.ShippingAddress;
import com.example.Secondhand.model.User;
import com.example.Secondhand.service.CartService;
import com.example.Secondhand.service.OrderService;
import com.example.Secondhand.service.ShippingAddressService;
import com.example.Secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShippingAddressService shippingAddressService;

    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        try {
            User user = userService.getCurrentUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            
            List<CartItem> cartItems = cartService.getCartItems(user);
            if (cartItems == null) {
                cartItems = new ArrayList<>();
            }
            
            // 计算总数量和小计
            int totalQuantity = 0;
            double subtotal = 0.0;
            
            for (CartItem item : cartItems) {
                if (item != null && item.getProduct() != null) {
                    totalQuantity += item.getQuantity();
                    subtotal += item.getTotal();
                }
            }
            
            System.out.println("Cart items: " + cartItems.size());
            System.out.println("Total quantity: " + totalQuantity);
            System.out.println("Subtotal: " + subtotal);
            
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("totalQuantity", totalQuantity);
            return "cart";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading cart: " + e.getMessage());
            return "cart";
        }
    }

    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable("id") Long productId, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        cartService.addToCart(productId, user);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (productId == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid product ID");
                return "redirect:/cart";
            }

            User user = userService.getCurrentUser(authentication);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please login first");
                return "redirect:/login";
            }
            
            cartService.removeFromCart(productId, user);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
            return "redirect:/cart";
        } catch (Exception e) {
            e.printStackTrace(); // 添加日志记录
            redirectAttributes.addFlashAttribute("error", "Failed to remove item: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/update/{id}")
    public String updateQuantity(@PathVariable("id") Long productId, 
                               @RequestParam int quantity,
                               Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        // Ensure quantity is at least 1
        if (quantity < 1) {
            quantity = 1;
        }
        cartService.updateQuantity(productId, user, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/payment-info")
    public String paymentInfo(Model model, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        List<CartItem> cartItems = cartService.getCartItems(user);
        double subtotal = cartService.calculateTotal(user);
        double tax = 1.0; // 10% tax
        double total = subtotal + tax;

        // 获取用户的默认地址和所有保存的地址
        ShippingAddress defaultAddress = shippingAddressService.getDefaultAddress(user);
        List<ShippingAddress> savedAddresses = shippingAddressService.getUserAddresses(user);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("savedAddresses", savedAddresses);
        return "payment-info";
    }

    @PostMapping("/order-success")
    public String orderSuccess(@RequestParam("fullName") String fullName,
                             @RequestParam("phone") String phone,
                             @RequestParam("address") String address,
                             @RequestParam(value = "deliveryNotes", required = false) String deliveryNotes,
                             Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        orderService.createOrder(user, fullName, phone, address, deliveryNotes);
        return "order-success";
    }
} 