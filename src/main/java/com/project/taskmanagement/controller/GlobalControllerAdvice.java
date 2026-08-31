package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.service.NotificationService;
import com.project.taskmanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final TeamService teamService;
    private final NotificationService notificationService;

    @ModelAttribute("userTeamsAlphabetical")
    public List<Team> getUserTeamsAlphabetical(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && principal.getUser() != null) {
            return teamService.getUserTeamsAlphabetical(principal.getUser().getId());
        }
        return List.of();
    }

    @ModelAttribute("userNotifications")
    public List<Notification> getUserNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && principal.getUser() != null) {
            return notificationService.getNotificationsForUser(principal.getUser().getId());
        }
        return List.of();
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && principal.getUser() != null) {
            return notificationService.countUnreadNotifications(principal.getUser().getId());
        }
        return 0L;
    }
}
