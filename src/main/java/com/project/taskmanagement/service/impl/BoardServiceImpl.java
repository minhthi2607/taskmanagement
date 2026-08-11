package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.BoardCreateDto;
import com.project.taskmanagement.dto.BoardUpdateDto;
import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.enums.Visibility;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.BoardService;
import com.project.taskmanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final BoardMemberRepository boardMemberRepository;
    private final BoardPermissionService boardPermissionService;

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
                .visibility(BoardVisibility.GROUP)
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
    /**
     * Story #20: Tạo mới board (Riêng tư/Nhóm/Công khai) qua DTO
     */
    @Override
    @Transactional
    public Board createBoard(BoardCreateDto dto, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để tạo bảng công việc!");
        }

        Long teamId = dto.getTeamId();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm với ID: " + teamId));

        if (!teamService.isUserMemberOfTeam(teamId, currentUser.getId())) {
            throw new AccessDeniedException("Bạn không phải là thành viên của nhóm này nên không thể tạo bảng!");
        }

        String boardName = dto.getName() != null ? dto.getName().trim() : "";
        if (boardName.isBlank()) {
            throw new IllegalArgumentException("Tên bảng công việc không được để trống!");
        }

        Board board = Board.builder()
                .teamId(teamId)
                .name(boardName)
                .visibility(dto.getVisibility())
                .createdBy(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Board savedBoard = boardRepository.save(board);

        BoardMember adminMember = BoardMember.builder()
                .boardId(savedBoard.getId())
                .userId(currentUser.getId())
                .role(Role.ADMIN)
                .joinedAt(LocalDateTime.now())
                .build();

        boardMemberRepository.save(adminMember);

        return savedBoard;
    }

    /**
     * Story #21, #22: Cập nhật thông tin Bảng qua BoardUpdateDto
     */
    @Override
    @Transactional
    public Board updateBoard(Long boardId, BoardUpdateDto dto, User currentUser) {
        boardPermissionService.checkAdminPermission(boardId, currentUser);

        Board board = getBoardById(boardId);

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            board.setName(dto.getName().trim());
        }

        if (dto.getVisibility() != null) {
            board.setVisibility(dto.getVisibility());
        }

        return boardRepository.save(board);
    }

    /**
     * Story #21: Đổi tên board (chỉ quản trị bảng)
     */
    @Override
    @Transactional
    public Board updateBoardName(Long boardId, String name, User currentUser) {
        boardPermissionService.checkAdminPermission(boardId, currentUser);

        Board board = getBoardById(boardId);
        String newName = (name != null) ? name.trim() : "";
        if (newName.isBlank()) {
            throw new IllegalArgumentException("Tên bảng không được để trống!");
        }

        board.setName(newName);
        return boardRepository.save(board);
    }

    /**
     * Story #22: Đổi visibility board (chỉ quản trị bảng)
     */
    @Override
    @Transactional
    public Board updateBoardVisibility(Long boardId, BoardVisibility visibility, User currentUser) {
        boardPermissionService.checkAdminPermission(boardId, currentUser);

        Board board = getBoardById(boardId);
        if (visibility == null) {
            throw new IllegalArgumentException("Quyền truy cập bảng không được để trống!");
        }

        board.setVisibility(visibility);
        return boardRepository.save(board);
    }

    /**
     * Story #27: Xóa board (chỉ quản trị viên/admin board)
     */
    @Override
    @Transactional
    public void deleteBoard(Long boardId, User currentUser) {
        boardPermissionService.checkAdminPermission(boardId, currentUser);

        Board board = getBoardById(boardId);
        boardRepository.delete(board);
    }

    @Override
    @Transactional(readOnly = true)
    public Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng với ID: " + boardId));
    }

    @Override
    @Transactional(readOnly = true)
    public Board getBoardDetail(Long boardId, User currentUser) {
        boardPermissionService.checkViewPermission(boardId, currentUser);
        return getBoardById(boardId);
    }

}
