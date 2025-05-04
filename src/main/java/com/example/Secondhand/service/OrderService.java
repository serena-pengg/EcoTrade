package com.example.Secondhand.service;

import com.example.Secondhand.model.Order;
import com.example.Secondhand.model.User;
import java.util.List;

public interface OrderService {
    List<Order> getUserOrders(User user);
    Order createOrder(User user, String fullName, String phone, String address, String deliveryNotes);
    List<Order> getOrdersByUser(User user);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getAllOrders();
    Order saveOrder(Order order);
} 