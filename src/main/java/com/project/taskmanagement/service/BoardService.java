package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.User;

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
}
