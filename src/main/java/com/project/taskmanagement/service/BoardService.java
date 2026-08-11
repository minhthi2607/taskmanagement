package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.BoardCreateDto;
import com.project.taskmanagement.dto.BoardUpdateDto;
import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;

import java.util.List;

public interface BoardService {

    /**
     * Tạo mới một Bảng công việc trong Nhóm (Story #19)
     */
    Board createBoard(Long teamId, String name, User currentUser);

    /**
     * Lấy danh sách các Bảng thuộc về Nhóm theo ID nhóm (Story #19)
     */
    List<Board> getBoardsByTeamId(Long teamId, User currentUser);
    /**
     * Story #20: Tạo mới một Bảng công việc trong Nhóm (dùng BoardCreateDto)
     */
    Board createBoard(BoardCreateDto dto, User currentUser);

    /**
     * Story #21, #22: Cập nhật thông tin Bảng công việc (Tên & Visibility) qua BoardUpdateDto
     */
    Board updateBoard(Long boardId, BoardUpdateDto dto, User currentUser);

    /**
     * Story #21: Đổi tên board (Chỉ quản trị bảng)
     */
    Board updateBoardName(Long boardId, String name, User currentUser);

    /**
     * Story #22: Đổi visibility board (Chỉ quản trị bảng)
     */
    Board updateBoardVisibility(Long boardId, BoardVisibility visibility, User currentUser);

    /**
     * Story #27: Xóa board (Chỉ quản trị viên/admin board)
     */
    void deleteBoard(Long boardId, User currentUser);

    /**
     * Lấy thông tin Bảng theo ID
     */
    Board getBoardById(Long boardId);

    /**
     * Lấy thông tin chi tiết Bảng có kiểm tra quyền xem
     */
    Board getBoardDetail(Long boardId, User currentUser);
}
