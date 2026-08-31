package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.CardTimeLogCreateDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.service.CardService;
import com.project.taskmanagement.service.CardTimeLogService;
import com.project.taskmanagement.service.TaskListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CardTimeLogController {

    private final CardTimeLogService cardTimeLogService;
    private final CardService cardService;
    private final TaskListService taskListService;

    /**
     * Story #55: Ghi nhận nhật ký thời gian (Time Log)
     */
    @PostMapping("/card/{cardId}/time-logs/add")
    public String addTimeLog(
            @PathVariable("cardId") Long cardId,
            @Valid @ModelAttribute("cardTimeLogCreateDto") CardTimeLogCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long boardId = null;
        try {
            Card card = cardService.getCardById(cardId);
            boardId = taskListService.getTaskListById(card.getTaskListId()).getBoardId();

            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
                return "redirect:/board/" + boardId;
            }

            cardTimeLogService.addTimeLog(cardId, dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Ghi nhận nhật ký thời gian thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #55: Xóa nhật ký thời gian
     */
    @PostMapping("/card/{cardId}/time-logs/{logId}/delete")
    public String deleteTimeLog(
            @PathVariable("cardId") Long cardId,
            @PathVariable("logId") Long logId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long boardId = null;
        try {
            Card card = cardService.getCardById(cardId);
            boardId = taskListService.getTaskListById(card.getTaskListId()).getBoardId();
            cardTimeLogService.deleteTimeLog(logId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhật ký thời gian thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }
}
