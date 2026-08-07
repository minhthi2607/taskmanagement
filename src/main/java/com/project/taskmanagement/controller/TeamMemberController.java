package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.service.TeamMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    /**
     * Story #16: Mời thành viên mới vào nhóm qua Email
     */
    @PostMapping("/team/{teamId}/members/invite")
    public String inviteMember(
            @PathVariable("teamId") Long teamId,
            @RequestParam("email") String email,
            @RequestParam(value = "role", defaultValue = "MEMBER") Role role,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để mời thành viên!");
            return "redirect:/auth/login";
        }

        try {
            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(null)
                    .build()
                    .toUriString();

            teamMemberService.inviteMember(teamId, email, role, principal.getUser(), baseUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi lời mời tham gia nhóm tới email " + email + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/team/" + teamId;
    }

    /**
     * Story #16: Chấp nhận lời mời qua Token từ Email
     */
    @GetMapping("/team/accept-invite")
    public String acceptInvite(
            @RequestParam("token") String token,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập tài khoản để chấp nhận lời mời!");
            return "redirect:/auth/login";
        }

        try {
            teamMemberService.acceptInvitation(token, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia nhóm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/team/list";
    }

    /**
     * Story #17: Loại thành viên ra khỏi nhóm
     */
    @PostMapping("/team/{teamId}/members/{userId}/remove")
    public String removeMember(
            @PathVariable("teamId") Long teamId,
            @PathVariable("userId") Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            teamMemberService.removeMember(teamId, targetUserId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã loại thành viên ra khỏi nhóm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/team/" + teamId;
    }

    /**
     * Story #18: Đổi quyền thành viên nhóm (Quản trị nhóm / Thành viên)
     */
    @PostMapping("/team/{teamId}/members/{userId}/role")
    public String updateMemberRole(
            @PathVariable("teamId") Long teamId,
            @PathVariable("userId") Long targetUserId,
            @RequestParam("role") Role role,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            teamMemberService.updateMemberRole(teamId, targetUserId, role, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền thành viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/team/" + teamId;
    }
}
