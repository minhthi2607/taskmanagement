package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.ChangePasswordDto;
import com.project.taskmanagement.dto.UserProfileDto;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //Đổi mật khẩu

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        if (!model.containsAttribute("changePasswordDto")) {
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            return "user/change-password";
        }

        try {
            userService.changePassword(principal.getUser(), changePasswordDto);

            // Đổi mật khẩu thành công -> Bắt buộc đăng nhập lại
            new SecurityContextLogoutHandler().logout(
                    request,
                    response,
                    SecurityContextHolder.getContext().getAuthentication()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại bằng mật khẩu mới.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/change-password";
        }
    }

    // Thay đổi thông tin cá nhân

    @GetMapping("/profile")
    public String showProfileForm(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (!model.containsAttribute("userProfileDto")) {
            User user = principal.getUser();
            UserProfileDto dto = UserProfileDto.builder()
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .phone(user.getPhone())
                    .avatarUrl(user.getAvatarUrl())
                    .build();
            model.addAttribute("userProfileDto", dto);
        }
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute("userProfileDto") UserProfileDto userProfileDto,
            BindingResult result,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Email không cho sửa -> đảm bảo luôn giữ email cũ của user
        userProfileDto.setEmail(principal.getUser().getEmail());

        if (result.hasErrors()) {
            userProfileDto.setAvatarUrl(principal.getUser().getAvatarUrl());
            return "user/profile";
        }

        try {
            User updatedUser = userService.updateProfile(principal.getUser(), userProfileDto, avatarFile);
            request.getSession().setAttribute("SESSION_USER", updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
            return "redirect:/user/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            userProfileDto.setAvatarUrl(principal.getUser().getAvatarUrl());
            return "user/profile";
        }
    }
}
