package com.example.Secondhand.repository;

import com.example.Secondhand.model.ShippingAddress;
import com.example.Secondhand.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    List<ShippingAddress> findByUser(User user);
    List<ShippingAddress> findByUserAndIsDefaultTrue(User user);
    void deleteByUser(User user);
} 