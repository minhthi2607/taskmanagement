package com.project.taskmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class LoginInputValidationFilter extends OncePerRequestFilter {

    // Regex kiểm tra định dạng email cơ bản
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ kiểm tra khi có request POST gửi tới đường dẫn /auth/login
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(request.getRequestURI())) {
            String email = request.getParameter("email");

            boolean isEmailInvalid = email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches();

            if (isEmailInvalid) {
                // Nếu email không hợp lệ, chặn lại và điều hướng về trang login kèm thông báo lỗi
                response.sendRedirect(request.getContextPath() + "/auth/login?invalidChar=true");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
