package com.project.taskmanagement.service;

import java.util.List;

public interface EmailService {

    /**
     * Gửi email thông báo cho danh sách thành viên khi một nhóm bị xóa (Story #15)
     */
    void sendTeamDeletionNotification(String teamName, List<String> memberEmails, String deleterName);

    /**
     * Gửi email mời tham gia nhóm (Story #16)
     */
    void sendTeamInvitationEmail(String toEmail, String teamName, String inviterName, String inviteUrl, String roleName);
}
