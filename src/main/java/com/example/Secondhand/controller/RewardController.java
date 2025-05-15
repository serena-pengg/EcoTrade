package com.example.Secondhand.controller;

import com.example.Secondhand.model.Reward;
import com.example.Secondhand.model.RewardRedemption;
import com.example.Secondhand.model.User;
import com.example.Secondhand.service.RewardService;
import com.example.Secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String rewardsPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.getUserByEmail(authentication.getName());
            List<Reward> rewards = rewardService.getAllRewards();
            List<RewardRedemption> redemptions = rewardService.getUserRedemptions(user);
            
            model.addAttribute("user", user);
            model.addAttribute("rewards", rewards);
            model.addAttribute("redemptions", redemptions);
            return "rewards";
        }
        return "redirect:/login";
    }

    @PostMapping("/redeem/{rewardId}")
    @ResponseBody
    public Map<String, Object> redeemReward(@PathVariable Long rewardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.getUserByEmail(authentication.getName());
            try {
                RewardRedemption redemption = rewardService.redeemReward(user, rewardId);
                return Map.of(
                    "success", true,
                    "message", "Reward redeemed successfully",
                    "redemption", redemption
                );
            } catch (Exception e) {
                return Map.of(
                    "success", false,
                    "message", e.getMessage()
                );
            }
        }
        return Map.of(
            "success", false,
            "message", "User not authenticated"
        );
    }
} 