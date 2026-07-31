package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.TeamCreateDto;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.Visibility;
import com.project.taskmanagement.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * Story #10: Hiển thị trang Form tạo mới 1 nhóm
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("teamCreateDto")) {
            model.addAttribute("teamCreateDto", new TeamCreateDto());
        }
        return "team/team-form";
    }

    /**
     * Story #10: Xử lý submit Form tạo mới 1 nhóm
     */
    @PostMapping("/create")
    public String handleCreateTeam(@Valid @ModelAttribute("teamCreateDto") TeamCreateDto dto,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal UserPrincipal principal,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            return "team/team-form";
        }

        try {
            User currentUser = (principal != null) ? principal.getUser() : null;
            Team createdTeam = teamService.createTeam(dto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo nhóm '" + createdTeam.getName() + "' thành công!");
            return "redirect:/team/" + createdTeam.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tạo nhóm: " + e.getMessage());
            return "redirect:/team/create";
        }
    }

    /**
     * Story #14: Xem danh sách nhóm đã tạo hoặc tham dự (Sắp xếp Alphabet)
     */
    @GetMapping("/list")
    public String listTeams(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        List<Team> teams;
        if (principal != null && principal.getUser() != null) {
            teams = teamService.getUserTeamsAlphabetical(principal.getUser().getId());
        } else {
            teams = teamService.getAllTeamsAlphabetical();
        }
        model.addAttribute("teams", teams);
        return "team/team-list";
    }

    /**
     * Story #11: Xem thông tin chi tiết của 1 nhóm
     */
    @GetMapping("/{id}")
    public String showTeamDetail(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserPrincipal principal,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            Team team = teamService.getTeamById(id);
            Long currentUserId = (principal != null && principal.getUser() != null) ? principal.getUser().getId() : null;

            // Kiểm tra quyền truy cập nếu là nhóm PRIVATE và có thông tin đăng nhập
            if (team.getVisibility() == Visibility.PRIVATE && currentUserId != null) {
                if (!teamService.isUserMemberOfTeam(id, currentUserId)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập nhóm riêng tư này!");
                    return "redirect:/team/list";
                }
            }

            boolean isMember = currentUserId != null && teamService.isUserMemberOfTeam(id, currentUserId);
            boolean isAdmin = currentUserId == null || (team.getCreatedBy().equals(currentUserId));

            model.addAttribute("team", team);
            model.addAttribute("isMember", isMember);
            model.addAttribute("isAdmin", isAdmin);

            return "team/team-detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/list";
        }
    }
}
