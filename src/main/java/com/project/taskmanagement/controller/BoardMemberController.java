package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.service.BoardMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class BoardMemberController {

    private final BoardMemberService boardMemberService;

    /**
     * Story #23: Mời thành viên vào bảng qua email
     */
    @PostMapping("/board/{boardId}/members/invite")
    public String inviteMember(
            @PathVariable("boardId") Long boardId,
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

            boardMemberService.inviteMember(boardId, email, role, principal.getUser(), baseUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi lời mời tham gia bảng tới email " + email + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #23: Chấp nhận lời mời tham gia bảng qua Token từ Email
     */
    @GetMapping("/board/accept-invite")
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
            boardMemberService.acceptInvitation(token, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia bảng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/";
    }

    /**
     * Story #24: Loại thành viên khỏi bảng
     */
    @PostMapping("/board/{boardId}/members/{userId}/remove")
    public String removeMember(
            @PathVariable("boardId") Long boardId,
            @PathVariable("userId") Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            boardMemberService.removeMember(boardId, targetUserId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã loại thành viên ra khỏi bảng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #26: Đổi quyền thành viên trong bảng
     */
    @PostMapping("/board/{boardId}/members/{userId}/role")
    public String updateMemberRole(
            @PathVariable("boardId") Long boardId,
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
            boardMemberService.updateMemberRole(boardId, targetUserId, role, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền thành viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #25: Tham gia bảng (join) tự động với bảng PUBLIC/GROUP
     */
    @PostMapping("/board/{boardId}/join")
    public String joinBoard(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tham gia bảng!");
            return "redirect:/auth/login";
        }

        try {
            boardMemberService.joinBoard(boardId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã tham gia bảng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }
}
