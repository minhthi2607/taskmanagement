package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.ChangePasswordDto;
import com.project.taskmanagement.dto.RegisterDto;
import com.project.taskmanagement.dto.UserProfileDto;
import com.project.taskmanagement.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    boolean existsByEmail(String email);
    User registerUser(RegisterDto registerDto);
    void changePassword(User user, ChangePasswordDto changePasswordDto);
    User updateProfile(User user, UserProfileDto userProfileDto, MultipartFile avatarFile);
}
