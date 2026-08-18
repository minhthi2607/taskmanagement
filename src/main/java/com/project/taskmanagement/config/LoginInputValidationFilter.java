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

    // Regex kiểm tra ký tự đặc biệt nguy hiểm: *, %, ', ", ;, --
    private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[*%'\";]|--");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ kiểm tra khi có request POST gửi tới đường dẫn /auth/login
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/auth/login".equals(request.getRequestURI())) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            boolean isEmailInvalid = email != null && INVALID_CHARACTERS_PATTERN.matcher(email).find();
            boolean isPasswordInvalid = password != null && INVALID_CHARACTERS_PATTERN.matcher(password).find();

            if (isEmailInvalid || isPasswordInvalid) {
                // Nếu chứa ký tự đặc biệt nguy hiểm, chặn lại và điều hướng về trang login kèm thông báo lỗi
                response.sendRedirect(request.getContextPath() + "/auth/login?invalidChar=true");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
