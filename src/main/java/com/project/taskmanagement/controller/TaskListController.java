package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.TaskListCreateDto;
import com.project.taskmanagement.entity.TaskList;
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
public class TaskListController {

    private final TaskListService taskListService;

    /**
     * Story #28: Tạo mới TaskList trong board
     */
    @PostMapping("/board/{boardId}/task-list/create")
    public String createTaskList(
            @PathVariable("boardId") Long boardId,
            @Valid @ModelAttribute("taskListCreateDto") TaskListCreateDto dto,
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
            dto.setBoardId(boardId);
            TaskList createdList = taskListService.createTaskList(dto, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Tạo danh sách '" + createdList.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/board/" + boardId;
    }

    /**
     * Story #29: Đổi tên TaskList
     */
    @PostMapping("/task-list/{id}/update-name")
    public String updateTaskListName(
            @PathVariable("id") Long taskListId,
            @RequestParam("name") String name,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long boardId = null;
        try {
            TaskList taskList = taskListService.getTaskListById(taskListId);
            boardId = taskList.getBoardId();
            taskListService.updateTaskListName(taskListId, name, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tên danh sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #30: Đổi vị trí TaskList (Hỗ trợ cả AJAX kéo-thả và Form submit thủ công)
     */
    @PostMapping("/task-list/{id}/update-position")
    @ResponseBody
    public Object updateTaskListPosition(
            @PathVariable("id") Long taskListId,
            @RequestParam("position") Integer position,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            RedirectAttributes redirectAttributes
    ) {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(requestedWith);

        if (principal == null || principal.getUser() == null) {
            if (isAjax) {
                return org.springframework.http.ResponseEntity.status(401)
                        .body(java.util.Map.of("success", false, "message", "Bạn cần đăng nhập!"));
            }
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long boardId = null;
        try {
            TaskList taskList = taskListService.getTaskListById(taskListId);
            boardId = taskList.getBoardId();
            taskListService.updateTaskListPosition(taskListId, position, principal.getUser());
            if (isAjax) {
                return org.springframework.http.ResponseEntity.ok(
                        java.util.Map.of("success", true, "message", "Cập nhật vị trí danh sách thành công!", "position", position));
            }
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vị trí danh sách thành công!");
        } catch (Exception e) {
            if (isAjax) {
                return org.springframework.http.ResponseEntity.badRequest()
                        .body(java.util.Map.of("success", false, "message", e.getMessage()));
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }

    /**
     * Story #31: Xóa TaskList (có popup xác nhận)
     */
    @PostMapping("/task-list/{id}/delete")
    public String deleteTaskList(
            @PathVariable("id") Long taskListId,
            @AuthenticationPrincipal UserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getUser() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện thao tác này!");
            return "redirect:/auth/login";
        }

        Long boardId = null;
        try {
            TaskList taskList = taskListService.getTaskListById(taskListId);
            boardId = taskList.getBoardId();
            taskListService.deleteTaskList(taskListId, principal.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return (boardId != null) ? "redirect:/board/" + boardId : "redirect:/";
    }
}
