package com.example.Secondhand.service.impl;

import com.example.Secondhand.model.*;
import com.example.Secondhand.repository.OrderRepository;
import com.example.Secondhand.service.OrderService;
import com.example.Secondhand.service.CartService;
import com.example.Secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Override
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Override
    @Transactional
    public Order createOrder(User user, String fullName, String phone, String address, String deliveryNotes) {
        Order order = new Order();
        order.setUser(user);
        order.setFullName(fullName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setDeliveryNotes(deliveryNotes);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        
        // 初始化orderItems列表
        order.setOrderItems(new ArrayList<>());
        
        // 获取购物车商品
        List<CartItem> cartItems = cartService.getCartItems(user);
        
        // 计算订单总金额
        double totalAmount = cartService.calculateTotal(user);
        order.setTotalAmount(totalAmount);
        
        // 计算环保积分
        int ecoPoints = 0;
        
        // 创建订单项并计算积分
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            
            if (cartItem.getProduct() != null) {
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                
                // 计算环保积分
                if (cartItem.getProduct().getEcoScore() != null) {
                    int basePoints = (int)(cartItem.getProduct().getEcoScore() * 50);
                    int quantityBonus = cartItem.getQuantity() * 10;
                    int itemEcoPoints = basePoints + quantityBonus;
                    ecoPoints += itemEcoPoints;
                }
            } else if (cartItem.getHainanProduct() != null) {
                orderItem.setHainanProduct(cartItem.getHainanProduct());
                orderItem.setPrice(cartItem.getHainanProduct().getPrice());
                
                // 计算环保积分
                if (cartItem.getHainanProduct().getEcoScore() != null) {
                    int basePoints = (int)(cartItem.getHainanProduct().getEcoScore() * 50);
                    int quantityBonus = cartItem.getQuantity() * 10;
                    int itemEcoPoints = basePoints + quantityBonus;
                    ecoPoints += itemEcoPoints;
                }
            }
            
            orderItem.setQuantity(cartItem.getQuantity());
            order.getOrderItems().add(orderItem);
        }
        
        // 更新用户的积分
        user.setEcoPoints(user.getEcoPoints() + ecoPoints);
        userService.updateUser(user);
        
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
} 