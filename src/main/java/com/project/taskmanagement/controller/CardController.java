package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.CardCreateDto;
import com.project.taskmanagement.dto.CardUpdateDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.service.CardService;
import com.project.taskmanagement.service.TaskListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final TaskListService taskListService;

    /**
     * Story #32: Tạo mới Card trong TaskList
     */
    @PostMapping("/task-list/{taskListId}/cards/create")
    public String createCard(
            @PathVariable("taskListId") Long taskListId,
            @Valid @ModelAttribute("cardCreateDto") CardCreateDto dto,
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
            boardId = taskListService.getTaskListById(taskListId).getBoardId();

            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
                redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
                return "redirect:/board/" + boardId;
            }

            Card createdCard = cardService.createCard(taskListId, dto.getTitle(), principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo thẻ '" + createdCard.getTitle() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #33: Đổi vị trí Card trong cùng TaskList
     */
    @PostMapping("/card/{cardId}/reorder")
    public String reorderCard(
            @PathVariable("cardId") Long cardId,
            @RequestParam("position") Integer position,
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
            cardService.reorderCardInList(cardId, position, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vị trí thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #34: Di chuyển Card sang TaskList khác (cùng board)
     */
    @PostMapping("/card/{cardId}/move")
    public String moveCard(
            @PathVariable("cardId") Long cardId,
            @RequestParam("targetTaskListId") Long targetTaskListId,
            @RequestParam("position") Integer position,
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
            cardService.moveCardToAnotherList(cardId, targetTaskListId, position, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Di chuyển thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #35: Sửa description của Card
     */
    @PostMapping("/card/{cardId}/update")
    public String updateCard(
            @PathVariable("cardId") Long cardId,
            @ModelAttribute("cardUpdateDto") CardUpdateDto dto,
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
            cardService.updateCardDetail(cardId, dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mô tả thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Gán thành viên vào thẻ
     */
    @PostMapping("/card/{cardId}/members/add")
    public String addCardMember(
            @PathVariable("cardId") Long cardId,
            @RequestParam("userId") Long userId,
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
            cardService.addCardMember(cardId, userId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Gán thành viên vào thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Gỡ thành viên khỏi thẻ
     */
    @PostMapping("/card/{cardId}/members/{userId}/remove")
    public String removeCardMember(
            @PathVariable("cardId") Long cardId,
            @PathVariable("userId") Long userId,
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
            cardService.removeCardMember(cardId, userId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ thành viên khỏi thẻ!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #49: Thêm bình luận vào thẻ
     */
    @PostMapping("/card/{cardId}/comments/add")
    public String addCardComment(
            @PathVariable("cardId") Long cardId,
            @RequestParam("content") String content,
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
            cardService.addCardComment(cardId, content, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm bình luận!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #50: Sửa bình luận của chính mình
     */
    @PostMapping("/card/comments/{commentId}/update")
    public String updateCardComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long boardId = null;
        try {
            var updatedComment = cardService.updateCardComment(commentId, content, principal.getUser());
            Card card = cardService.getCardById(updatedComment.getCardId());
            boardId = taskListService.getTaskListById(card.getTaskListId()).getBoardId();
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bình luận thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #51: Xóa bình luận
     */
    @PostMapping("/card/comments/{commentId}/delete")
    public String deleteCardComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("cardId") Long cardId,
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
            cardService.deleteCardComment(commentId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bình luận!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #36: Đính kèm tệp vào thẻ
     */
    @PostMapping("/card/{cardId}/attachments/add")
    public String addAttachment(
            @PathVariable("cardId") Long cardId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
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
            cardService.addAttachment(cardId, file, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Tải lên tệp đính kèm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #36: Xóa tệp đính kèm
     */
    @PostMapping("/card/attachments/{attachmentId}/delete")
    public String deleteAttachment(
            @PathVariable("attachmentId") Long attachmentId,
            @RequestParam("cardId") Long cardId,
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
            cardService.deleteAttachment(attachmentId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tệp đính kèm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }
}
