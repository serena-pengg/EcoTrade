package com.example.Secondhand.repository;

import com.example.Secondhand.model.RewardRedemption;
import com.example.Secondhand.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {
    List<RewardRedemption> findByUserOrderByRedemptionDateDesc(User user);
} 