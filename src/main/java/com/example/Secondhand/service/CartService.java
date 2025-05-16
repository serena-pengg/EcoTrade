package com.example.Secondhand.service;

import com.example.Secondhand.model.CartItem;
import com.example.Secondhand.model.Product;
import com.example.Secondhand.model.User;
import com.example.Secondhand.model.HainanProduct;
import com.example.Secondhand.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private HainanProductService hainanProductService;

    @Autowired
    private UserService userService;

    public List<CartItem> getCartItems(User user) {
        return cartRepository.findByUser(user);
    }

    @Transactional
    public void addToCart(Long productId, User user, boolean isHainanProduct) {
        if (user == null) {
            throw new RuntimeException("User must be logged in to add items to cart");
        }

        CartItem existingItem;
        if (isHainanProduct) {
            HainanProduct product = hainanProductService.getProductById(productId);
            if (product == null) {
                throw new RuntimeException("Hainan product not found with id: " + productId);
            }
            existingItem = cartRepository.findByUserAndHainanProductId(user, productId);
            
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                cartRepository.save(existingItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setHainanProduct(product);
                newItem.setUser(user);
                newItem.setQuantity(1);
                cartRepository.save(newItem);
            }
        } else {
            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new RuntimeException("Product not found with id: " + productId);
            }
            existingItem = cartRepository.findByUserAndProductId(user, productId);
            
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                cartRepository.save(existingItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setUser(user);
                newItem.setQuantity(1);
                cartRepository.save(newItem);
            }
        }
    }

    @Transactional
    public void removeFromCart(Long productId, User user) {
        if (user == null) {
            throw new RuntimeException("User must be logged in");
        }
        
        if (productId == null) {
            throw new RuntimeException("Product ID cannot be null");
        }
        
        CartItem item = cartRepository.findByUserAndProductId(user, productId);
        if (item == null) {
            throw new RuntimeException("Item not found in cart");
        }
        
        try {
            cartRepository.delete(item);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to remove item from cart: " + e.getMessage());
        }
    }

    @Transactional
    public void updateQuantity(Long productId, User user, int quantity) {
        if (user == null) {
            throw new RuntimeException("User must be logged in");
        }
        
        if (productId == null) {
            throw new RuntimeException("Product ID cannot be null");
        }
        
        if (quantity < 1) {
            quantity = 1;
        }
        
        CartItem item = cartRepository.findByUserAndProductId(user, productId);
        if (item == null) {
            throw new RuntimeException("Item not found in cart");
        }
        
        try {
            item.setQuantity(quantity);
            cartRepository.save(item);
            cartRepository.flush(); // 确保更改立即写入数据库
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update quantity: " + e.getMessage());
        }
    }

    public double calculateTotal(User user) {
        List<CartItem> items = getCartItems(user);
        return items.stream()
                .mapToDouble(CartItem::getTotal)
                .sum();
    }

    public int calculateTotalQuantity(User user) {
        if (user == null) {
            return 0;
        }
        
        List<CartItem> items = getCartItems(user);
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int total = 0;
        for (CartItem item : items) {
            if (item != null && item.getQuantity() > 0) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    public void clearCart(User user) {
        List<CartItem> cartItems = cartRepository.findByUser(user);
        cartRepository.deleteAll(cartItems);
    }
} 