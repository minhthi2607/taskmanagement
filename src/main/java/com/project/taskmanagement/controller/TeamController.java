package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.TeamCreateDto;
import com.project.taskmanagement.dto.TeamUpdateDto;
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
    public String showCreateForm(@AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes, Model model) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tạo nhóm!");
            return "redirect:/auth/login";
        }
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
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tạo nhóm!");
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            return "team/team-form";
        }

        try {
            User currentUser = principal.getUser();
            Team createdTeam = teamService.createTeam(dto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo nhóm '" + createdTeam.getName() + "' thành công!");
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
            teams = teamService.getPublicTeamsAlphabetical();
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
            Long currentUserId = (principal != null && principal.getUser() != null) ? principal.getUser().getId()
                    : null;
            boolean isMember = currentUserId != null && teamService.isUserMemberOfTeam(id, currentUserId);
            boolean isAdmin = currentUserId != null && teamService.isUserAdminOfTeam(id, currentUserId);

            // Kiểm tra quyền truy cập nếu là nhóm PRIVATE
            if (team.getVisibility() == Visibility.PRIVATE && !isMember) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập nhóm riêng tư này!");
                return "redirect:/team/list";
            }

            model.addAttribute("team", team);
            model.addAttribute("isMember", isMember);
            model.addAttribute("isAdmin", isAdmin);

            return "team/team-detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/list";
        }
    }

    /**
     * Story #12, #13: Hiển thị trang Form chỉnh sửa thông tin & quyền riêng tư nhóm
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Team team = teamService.getTeamById(id);
            Long currentUserId = (principal != null && principal.getUser() != null) ? principal.getUser().getId()
                    : null;

            if (!teamService.isUserAdminOfTeam(id, currentUserId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa nhóm này!");
                return "redirect:/team/" + id;
            }

            if (!model.containsAttribute("teamUpdateDto")) {
                TeamUpdateDto dto = TeamUpdateDto.builder()
                        .name(team.getName())
                        .type(team.getType())
                        .visibility(team.getVisibility())
                        .description(team.getDescription())
                        .build();
                model.addAttribute("teamUpdateDto", dto);
            }

            model.addAttribute("team", team);
            return "team/team-edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/list";
        }
    }

    /**
     * Story #12, #13: Xử lý submit Form chỉnh sửa thông tin & quyền riêng tư nhóm
     */
    @PostMapping("/{id}/edit")
    public String handleEditTeam(@PathVariable("id") Long id,
            @Valid @ModelAttribute("teamUpdateDto") TeamUpdateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            Team team = teamService.getTeamById(id);
            model.addAttribute("team", team);
            return "team/team-edit";
        }

        try {
            User currentUser = (principal != null) ? principal.getUser() : null;
            Team updatedTeam = teamService.updateTeam(id, dto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin nhóm '" + updatedTeam.getName() + "' thành công!");
            return "redirect:/team/" + updatedTeam.getId();
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật nhóm: " + e.getMessage());
            return "redirect:/team/" + id + "/edit";
        }
    }

    /**
     * Story #15: Xử lý xóa nhóm
     */
    @PostMapping("/{id}/delete")
    public String handleDeleteTeam(@PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (principal != null) ? principal.getUser() : null;
            Team team = teamService.getTeamById(id);
            String teamName = team.getName();
            teamService.deleteTeam(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nhóm '" + teamName + "' thành công!");
            return "redirect:/team/list";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa nhóm: " + e.getMessage());
            return "redirect:/team/" + id;
        }
    }
}
