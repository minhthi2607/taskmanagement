package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * Story #19: Tạo mới Bảng công việc trong Nhóm
     */
    @PostMapping("/team/{teamId}/boards/create")
    public String createBoard(
            @PathVariable("teamId") Long teamId,
            @RequestParam("name") String name,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tạo bảng công việc!");
            return "redirect:/auth/login";
        }

        try {
            Board createdBoard = boardService.createBoard(teamId, name, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo bảng công việc '" + createdBoard.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/team/" + teamId;
    }
}
