package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.entity.Team;
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

    @ModelAttribute("userTeamsAlphabetical")
    public List<Team> getUserTeamsAlphabetical(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null && principal.getUser() != null) {
            if (principal.getUser().isSystemAdmin()) {
                return teamService.getAllTeamsAlphabetical();
            }
            return teamService.getUserTeamsAlphabetical(principal.getUser().getId());
        }
        return List.of();
    }
}
