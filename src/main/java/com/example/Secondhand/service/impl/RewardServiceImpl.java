package com.example.Secondhand.service.impl;

import com.example.Secondhand.model.Reward;
import com.example.Secondhand.model.RewardRedemption;
import com.example.Secondhand.model.User;
import com.example.Secondhand.repository.RewardRepository;
import com.example.Secondhand.repository.RewardRedemptionRepository;
import com.example.Secondhand.service.RewardService;
import com.example.Secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RewardRedemptionRepository redemptionRepository;

    @Autowired
    private UserService userService;

    @Override
    public List<Reward> getAllRewards() {
        return rewardRepository.findAll();
    }

    @Override
    @Transactional
    public RewardRedemption redeemReward(User user, Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("Reward not found"));

        // 检查库存
        if (reward.getStockQuantity() <= 0) {
            throw new RuntimeException("Reward is out of stock");
        }

        // 检查用户积分是否足够
        if (user.getEcoPoints() < reward.getPointsRequired()) {
            throw new RuntimeException("Insufficient eco points");
        }

        // 创建兑换记录
        RewardRedemption redemption = new RewardRedemption();
        redemption.setUser(user);
        redemption.setReward(reward);
        redemption.setPointsSpent(reward.getPointsRequired());
        redemption.setRedemptionDate(LocalDateTime.now());
        redemption.setStatus("PENDING");

        // 扣除用户积分
        user.setEcoPoints(user.getEcoPoints() - reward.getPointsRequired());
        userService.updateUser(user);

        // 减少奖励库存
        reward.setStockQuantity(reward.getStockQuantity() - 1);
        rewardRepository.save(reward);

        // 保存兑换记录
        return redemptionRepository.save(redemption);
    }

    @Override
    public List<RewardRedemption> getUserRedemptions(User user) {
        return redemptionRepository.findByUserOrderByRedemptionDateDesc(user);
    }
} 