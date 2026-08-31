package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    /**
     * Method CỐT LÕI, dùng chung cho mọi nơi cần bắn thông báo.
     * content phải được build sẵn hoàn chỉnh bởi nơi gọi (mục 14.1 GEMINI.md) —
     * method này không tự dựng câu, chỉ nhận và lưu.
     */
    Notification createNotification(Long userId, NotificationType type, String content, String link);

    List<Notification> getNotificationsForUser(Long userId);

    long countUnreadNotifications(Long userId);

    Notification markAsRead(Long notificationId, User currentUser);

    void markAllAsRead(User currentUser);
}
