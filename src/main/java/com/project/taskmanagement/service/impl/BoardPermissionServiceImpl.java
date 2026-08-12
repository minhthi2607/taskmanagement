package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardPermissionServiceImpl implements BoardPermissionService {

    private final BoardMemberRepository boardMemberRepository;
    private final BoardRepository boardRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public void checkAdminPermission(Long boardId, User user) {
        if (user == null || user.getId() == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Không tìm thấy bảng với ID: " + boardId);
        }

        boolean isAdmin = boardMemberRepository.existsByBoardIdAndUserIdAndRole(boardId, user.getId(), Role.ADMIN);
        if (!isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền quản trị viên (ADMIN) trên bảng này!");
        }
    }

    @Override
    public boolean isBoardAdmin(Long boardId, Long userId) {
        if (boardId == null || userId == null) {
            return false;
        }
        return boardMemberRepository.existsByBoardIdAndUserIdAndRole(boardId, userId, Role.ADMIN);
    }

    @Override
    public boolean isBoardMember(Long boardId, Long userId) {
        if (boardId == null || userId == null) {
            return false;
        }
        return boardMemberRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    @Override
    public void checkViewPermission(Long boardId, User user) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng với ID: " + boardId));

        BoardVisibility visibility = board.getVisibility();

        // PUBLIC -> Ai cũng xem được
        if (visibility == BoardVisibility.PUBLIC) {
            return;
        }

        if (user == null || user.getId() == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để xem bảng này!");
        }

        Long userId = user.getId();

        // GROUP -> BoardMember hoặc TeamMember
        if (visibility == BoardVisibility.GROUP) {
            boolean isBoardMem = boardMemberRepository.existsByBoardIdAndUserId(boardId, userId);
            boolean isTeamMem = teamMemberRepository.existsByTeamIdAndUserId(board.getTeamId(), userId);
            if (!isBoardMem && !isTeamMem) {
                throw new AccessDeniedException("Bạn không có quyền xem bảng nhóm này!");
            }
            return;
        }

        // PRIVATE -> Chỉ BoardMember
        if (visibility == BoardVisibility.PRIVATE) {
            boolean isBoardMem = boardMemberRepository.existsByBoardIdAndUserId(boardId, userId);
            if (!isBoardMem) {
                throw new AccessDeniedException("Đây là bảng riêng tư. Bạn không có quyền truy cập!");
            }
        }
    }
}
