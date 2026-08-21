package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Board;

public interface BoardMemberSyncService {

    /**
     * Đồng bộ tất cả TeamMember của Team vào Board (BoardVisibility.GROUP).
     * Dùng khi tạo board GROUP mới hoặc khi đổi visibility của board sang GROUP.
     */
    void syncBoardMembersForGroupBoard(Board board);

    /**
     * Tự động thêm một user mới gia nhập Team vào tất cả các board GROUP của Team đó.
     */
    void addMemberToAllGroupBoardsOfTeam(Long teamId, Long userId);

    /**
     * Tự động xóa một user bị loại/rời khỏi Team ra khỏi tất cả các board GROUP của Team đó.
     */
    void removeMemberFromAllGroupBoardsOfTeam(Long teamId, Long userId);
}
