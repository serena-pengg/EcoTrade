package com.example.Secondhand.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private String password;
    private String confirmPassword;
    private String username;
} 