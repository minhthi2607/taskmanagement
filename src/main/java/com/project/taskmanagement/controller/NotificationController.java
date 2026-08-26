package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Story #44-48: Đánh dấu 1 thông báo đã đọc, điều hướng tới link của thông báo đó
     */
    @PostMapping("/notifications/{id}/read")
    public String markAsRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        String link = "/";
        try {
            var notification = notificationService.markAsRead(id, principal.getUser());
            if (notification.getLink() != null && !notification.getLink().isBlank()) {
                link = notification.getLink();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/");
        }

        return "redirect:" + link;
    }

    /**
     * Story #44-48: Đánh dấu tất cả thông báo của người dùng hiện tại đã đọc
     */
    @PostMapping("/notifications/read-all")
    public String markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        String referer = request.getHeader("Referer");
        try {
            notificationService.markAllAsRead(principal.getUser());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + (referer != null ? referer : "/");
    }
}
