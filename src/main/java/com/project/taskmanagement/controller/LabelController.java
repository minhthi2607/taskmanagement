package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.entity.Label;
import com.project.taskmanagement.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/board/{boardId}/labels/create")
    public String createLabel(
            @PathVariable("boardId") Long boardId,
            @RequestParam("name") String name,
            @RequestParam("color") String color,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            labelService.createLabel(boardId, name, color, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo nhãn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    @PostMapping("/label/{labelId}/update")
    public String updateLabel(
            @PathVariable("labelId") Long labelId,
            @RequestParam("boardId") Long boardId,
            @RequestParam("name") String name,
            @RequestParam("color") String color,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            labelService.updateLabel(labelId, name, color, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhãn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    @PostMapping("/label/{labelId}/delete")
    public String deleteLabel(
            @PathVariable("labelId") Long labelId,
            @RequestParam("boardId") Long boardId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        try {
            labelService.deleteLabel(labelId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhãn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }
}
