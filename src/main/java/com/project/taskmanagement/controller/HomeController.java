package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.dto.TeamBoardsDto;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.service.BoardService;
import com.project.taskmanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BoardService boardService;
    private final TeamService teamService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal != null && principal.getUser() != null) {
            User currentUser = principal.getUser();
            try {
                // Story #4: Bảng do user tự tạo, group theo Team, sắp xếp Alphabet
                List<TeamBoardsDto> createdTeamBoards = boardService.getCreatedBoardsGroupedByTeamAlphabetical(currentUser.getId());
                model.addAttribute("createdTeamBoards", createdTeamBoards);

                // Story #5: Bảng user được gán làm thành viên (ngoại trừ tự tạo), group theo Team, sắp xếp Alphabet
                List<TeamBoardsDto> joinedTeamBoards = boardService.getJoinedBoardsGroupedByTeamAlphabetical(currentUser.getId());
                model.addAttribute("joinedTeamBoards", joinedTeamBoards);

                // Danh sách Nhóm của User (Giữ tương thích ngược)
                model.addAttribute("userTeamsAlphabetical", teamService.getUserTeamsAlphabetical(currentUser.getId()));
            } catch (Exception e) {
                log.error("Lỗi khi tải dữ liệu trang chủ cho user: {}", currentUser.getEmail(), e);
                model.addAttribute("errorMessage", "Không thể tải dữ liệu trang chủ. Vui lòng thử lại sau!");
            }
        }
        return "index";
    }
}
