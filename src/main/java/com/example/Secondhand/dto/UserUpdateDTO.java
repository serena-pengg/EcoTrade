package com.example.Secondhand.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String phoneNumber;
    private String address;
    private String bio;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
} 