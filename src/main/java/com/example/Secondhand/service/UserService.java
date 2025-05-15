package com.example.Secondhand.service;

import com.example.Secondhand.dto.UserDTO;
import com.example.Secondhand.dto.UserUpdateDTO;
import com.example.Secondhand.model.User;
import org.springframework.security.core.Authentication;

public interface UserService {
    User registerUser(UserDTO userDTO);
    Authentication login(String email, String password);
    User getCurrentUser();
    User getCurrentUser(Authentication authentication);
    User updateUserProfile(Long userId, UserUpdateDTO userUpdateDTO);
    User findByUsername(String username);
    User updateUser(User user);
    User getUserByEmail(String email);
    User getUserById(Long id);
    void recalculateEcoPoints(User user);
    void recalculateAllUsersEcoPoints();
} 