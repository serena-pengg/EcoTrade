package com.example.Secondhand.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "hainan_product_id")
    private HainanProduct hainanProduct;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int quantity;

    @Transient
    public double getTotal() {
        if (product != null) {
            return product.getPrice() * quantity;
        } else if (hainanProduct != null) {
            return hainanProduct.getPrice() * quantity;
        }
        return 0;
    }
} 