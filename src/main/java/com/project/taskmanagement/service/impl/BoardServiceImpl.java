package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.User;
// import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Visibility;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.service.BoardService;
import com.project.taskmanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    @Override
    @Transactional
    public Board createBoard(Long teamId, String name, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để tạo bảng công việc!");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm với ID: " + teamId));

        // Kiểm tra xem người dùng có phải thành viên hoặc admin của nhóm không
        if (!teamService.isUserMemberOfTeam(teamId, currentUser.getId())) {
            throw new SecurityException(
                    "Chỉ thành viên hoặc Quản trị nhóm mới có quyền tạo bảng công việc trong nhóm này!");
        }

        String boardName = name != null ? name.trim() : "";
        if (boardName.isBlank()) {
            throw new IllegalArgumentException("Tên bảng công việc không được để trống!");
        }

        Board board = Board.builder()
                .teamId(teamId)
                .name(boardName)
                .createdBy(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        return boardRepository.save(board);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Board> getBoardsByTeamId(Long teamId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm với ID: " + teamId));

        Long userId = currentUser != null ? currentUser.getId() : null;

        // Nếu nhóm là PRIVATE -> Bắt buộc user phải là thành viên
        if (team.getVisibility() == Visibility.PRIVATE) {
            if (userId == null || !teamService.isUserMemberOfTeam(teamId, userId)) {
                throw new SecurityException("Bạn không có quyền xem các bảng của nhóm riêng tư này!");
            }
        }

        return boardRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }
}
