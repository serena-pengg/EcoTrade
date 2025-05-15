package com.example.Secondhand.service;

import com.example.Secondhand.model.Reward;
import com.example.Secondhand.model.RewardRedemption;
import com.example.Secondhand.model.User;

import java.util.List;

public interface RewardService {
    List<Reward> getAllRewards();
    
    RewardRedemption redeemReward(User user, Long rewardId);
    
    List<RewardRedemption> getUserRedemptions(User user);
} 