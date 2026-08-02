package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.ChangePasswordDto;
import com.project.taskmanagement.dto.RegisterDto;
import com.project.taskmanagement.dto.UserProfileDto;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @org.springframework.beans.factory.annotation.Value("${app.upload-dir:uploads/}")
    private String uploadDirConfig;

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional
    public User registerUser(RegisterDto registerDto) {
        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());

        User user = User.builder()
                .email(registerDto.getEmail().trim().toLowerCase())
                .displayName(registerDto.getDisplayName().trim())
                .phone(registerDto.getPhone() != null ? registerDto.getPhone().trim() : null)
                .password(encodedPassword)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordDto changePasswordDto) {
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }

        // Kiểm tra mật khẩu mới không trùng mật khẩu cũ
        if (changePasswordDto.getNewPassword().equals(changePasswordDto.getCurrentPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        // Kiểm tra mật khẩu xác nhận
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        // Đổi mật khẩu
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(User user, UserProfileDto dto, MultipartFile avatarFile) {
        // Cập nhật tên hiển thị & số điện thoại
        user.setDisplayName(dto.getDisplayName().trim());
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);

        // Xử lý upload avatar nếu có tệp được tải lên
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // [BỔ SUNG 1] Kiểm tra dung lượng (tối đa 2MB)
            if (avatarFile.getSize() > 2 * 1024 * 1024) {
                throw new IllegalArgumentException("Dung lượng ảnh đại diện không được vượt quá 2MB.");
            }
            // Kiểm tra định dạng MIME type
            String contentType = avatarFile.getContentType();
            if (contentType == null
                    || !java.util.List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
                throw new IllegalArgumentException("Chỉ chấp nhận tệp ảnh định dạng JPG, PNG hoặc WEBP.");
            }

            String originalFileName = StringUtils.cleanPath(
                    avatarFile.getOriginalFilename() != null ? avatarFile.getOriginalFilename() : "avatar.png");
            String extension = "";
            int dotIndex = originalFileName.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = originalFileName.substring(dotIndex);
            }

            String newFileName = UUID.randomUUID().toString() + extension;
            Path uploadDir = Paths.get(uploadDirConfig);

            try {
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                try (InputStream inputStream = avatarFile.getInputStream()) {
                    Path filePath = uploadDir.resolve(newFileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                user.setAvatarUrl("/uploads/" + newFileName);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu tệp ảnh avatar. Vui lòng thử lại.", e);
            }
        }

        User updatedUser = userRepository.save(user);


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            principal.getUser().setDisplayName(updatedUser.getDisplayName());
            principal.getUser().setPhone(updatedUser.getPhone());
            principal.getUser().setAvatarUrl(updatedUser.getAvatarUrl());
        }

        return updatedUser;
    }
}
