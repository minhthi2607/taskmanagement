package com.project.taskmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Chống Open Redirect (issue #26): dựng redirect an toàn từ header Referer,
 * chỉ chấp nhận đường dẫn tương đối hoặc URL tuyệt đối cùng host/port với server.
 */
@Slf4j
@Component
public class SafeRedirectHelper {

    public String getSafeRedirectUrl(HttpServletRequest request, String defaultUrl) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "redirect:" + defaultUrl;
        }

        // Chấp nhận đường dẫn tương đối
        if (referer.startsWith("/") && !referer.startsWith("//")) {
            return "redirect:" + referer;
        }

        try {
            java.net.URI uri = new java.net.URI(referer);
            String host = uri.getHost();
            int port = uri.getPort();

            // Kiểm tra host/port trùng với ứng dụng
            if (host != null && host.equals(request.getServerName())) {
                if (port == -1 || port == request.getServerPort()) {
                    return "redirect:" + referer;
                }
            }
        } catch (Exception e) {
            log.debug("Invalid referer URL: {}", referer);
        }

        log.warn("Chặn redirect không an toàn, referer: {}", referer);
        return "redirect:" + defaultUrl;
    }
}
