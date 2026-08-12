package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.BoardCreateDto;
import com.project.taskmanagement.dto.BoardUpdateDto;
import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.service.BoardService;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.BoardMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardPermissionService boardPermissionService;
    private final BoardMemberService boardMemberService;

    /**
     * Story #20: Tạo mới Bảng công việc từ DTO (Task B)
     */
    @PostMapping("/board/create")
    public String createBoardWithDto(
            @Valid @ModelAttribute("boardCreateDto") BoardCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tạo bảng công việc!");
            return "redirect:/auth/login";
        }

        if (bindingResult != null && bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/team/" + dto.getTeamId();
        }

        try {
            Board createdBoard = boardService.createBoard(dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo bảng công việc '" + createdBoard.getName() + "' thành công!");
            return "redirect:/board/" + createdBoard.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/" + (dto != null ? dto.getTeamId() : "");
        }
    }

    /**
     * Hiển thị chi tiết bảng công việc
     */
    @GetMapping("/board/{id}")
    public String showBoardDetail(
            @PathVariable("id") Long boardId,
            @AuthenticationPrincipal UserPrincipal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = (principal != null) ? principal.getUser() : null;
        try {
            Board board = boardService.getBoardDetail(boardId, currentUser);
            Long userId = (currentUser != null) ? currentUser.getId() : null;

            boolean isBoardAdmin = boardPermissionService.isBoardAdmin(boardId, userId);
            boolean isBoardMember = boardPermissionService.isBoardMember(boardId, userId);

            if (!model.containsAttribute("boardUpdateDto")) {
                model.addAttribute("boardUpdateDto", BoardUpdateDto.builder()
                        .name(board.getName())
                        .visibility(board.getVisibility())
                        .build());
            }

            model.addAttribute("board", board);
            model.addAttribute("isBoardAdmin", isBoardAdmin);
            model.addAttribute("isBoardMember", isBoardMember);
            model.addAttribute("allVisibilities", BoardVisibility.values());
            model.addAttribute("boardMembers", boardMemberService.getBoardMembers(boardId));
            model.addAttribute("currentUser", currentUser);

            return "board/board-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/team/list";
        }
    }

    /**
     * Story #21 & #22: Cập nhật thông tin bảng qua BoardUpdateDto
     */
    @PostMapping("/board/{id}/edit")
    public String updateBoard(
            @PathVariable("id") Long boardId,
            @Valid @ModelAttribute("boardUpdateDto") BoardUpdateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/board/" + boardId;
        }

        try {
            Board updatedBoard = boardService.updateBoard(boardId, dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bảng công việc '" + updatedBoard.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #21: Đổi tên board (chỉ quản trị bảng)
     */
    @PostMapping("/board/{id}/update-name")
    public String updateBoardName(
            @PathVariable("id") Long boardId,
            @RequestParam("name") String name,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            Board updatedBoard = boardService.updateBoardName(boardId, name, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tên bảng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #22: Đổi visibility board (chỉ quản trị bảng)
     */
    @PostMapping("/board/{id}/update-visibility")
    public String updateBoardVisibility(
            @PathVariable("id") Long boardId,
            @RequestParam("visibility") BoardVisibility visibility,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            Board updatedBoard = boardService.updateBoardVisibility(boardId, visibility, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền riêng tư bảng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #27: Xóa board (chỉ quản trị viên/admin board)
     */
    @PostMapping("/board/{id}/delete")
    public String deleteBoard(
            @PathVariable("id") Long boardId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long teamId = null;
        try {
            Board board = boardService.getBoardById(boardId);
            teamId = board.getTeamId();
            boardService.deleteBoard(boardId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bảng '" + board.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (teamId != null) {
                return "redirect:/board/" + boardId;
            }
            return "redirect:/team/list";
        }

        return "redirect:/team/" + teamId;
    }
}
