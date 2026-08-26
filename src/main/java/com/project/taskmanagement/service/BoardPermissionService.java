package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.User;

public interface BoardPermissionService {

    /**
     * Kiểm tra user có phải là ADMIN của Board hay không.
     * Nếu không phải ADMIN hoặc chưa đăng nhập, throw AccessDeniedException.
     */
    void checkAdminPermission(Long boardId, User user);

    /**
     * Kiểm tra xem user có phải là ADMIN của Board hay không (trả về boolean).
     */
    boolean isBoardAdmin(Long boardId, Long userId);

    /**
     * Kiểm tra xem user có phải là thành viên của Board hay không (trả về boolean).
     */
    boolean isBoardMember(Long boardId, Long userId);

    /**
     * Kiểm tra xem user có quyền xem Board theo quy tắc 2 lớp (PRIVATE/GROUP/PUBLIC).
     * Throw AccessDeniedException nếu không có quyền xem.
     */
    void checkViewPermission(Long boardId, User user);

    /**
     * Kiểm tra xem user có quyền SỬA Board (tạo/sửa/xóa TaskList, Card...).
     * Theo quy tắc mục 7.2: Phải là BoardMember của bảng.
     */
    void checkEditPermission(Long boardId, User user);

    /**
     * Trả về true nếu user có quyền SỬA Board (đã là BoardMember).
     */
    boolean canEditBoard(Long boardId, Long userId);

    /**
     * Kiểm tra quyền thao tác trên CardMember và Label (Story #37, #43).
     */
    void checkCardMemberOrLabelPermission(Long boardId, User user);
}
