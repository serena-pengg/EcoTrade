package com.example.Secondhand.task;

import com.example.Secondhand.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EcoPointsUpdateTask {

    @Autowired
    private UserService userService;

    // 每天凌晨2点执行更新
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateAllUsersEcoPoints() {
        userService.recalculateAllUsersEcoPoints();
    }
} 