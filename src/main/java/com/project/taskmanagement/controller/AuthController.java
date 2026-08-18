package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.RegisterDto;
import com.project.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Kiểm tra mật khẩu xác nhận
        if (registerDto.getPassword() != null && registerDto.getConfirmPassword() != null
                && !registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registerDto", "Mật khẩu xác nhận không khớp.");
        }

        // Kiểm tra trùng Email
        if (registerDto.getEmail() != null && !registerDto.getEmail().isBlank()
                && userService.existsByEmail(registerDto.getEmail())) {
            result.rejectValue("email", "error.registerDto", "Email này đã được sử dụng. Vui lòng chọn email khác.");
        }

        // Nếu có lỗi validation -> trả về trang đăng ký
        if (result.hasErrors()) {
            return "auth/register";
        }

        // Lưu người dùng vào CSDL
        userService.registerUser(registerDto);

        // Gửi thông báo thành công và chuyển hướng tới trang Đăng nhập
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        redirectAttributes.addAttribute("registered", true);
        redirectAttributes.addAttribute("email", registerDto.getEmail());

        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "registered", required = false) Boolean registered,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("registered", Boolean.TRUE.equals(registered));
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);

        return "auth/login";
    }
}
