package com.example.Secondhand.repository;

import com.example.Secondhand.model.CartItem;
import com.example.Secondhand.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    
    @Query("SELECT c FROM CartItem c WHERE c.user = :user AND c.product.id = :productId")
    CartItem findByUserAndProductId(@Param("user") User user, @Param("productId") Long productId);
    
    void deleteByUser(User user);
} 