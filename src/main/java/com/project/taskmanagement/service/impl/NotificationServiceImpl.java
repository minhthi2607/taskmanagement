package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.NotificationRepository;
import com.project.taskmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(Long userId, NotificationType type, String content, String link) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .content(content)
                .link(link)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + notificationId));

        if (!Objects.equals(notification.getUserId(), currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền thao tác với thông báo này!");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return notification;
    }

    @Override
    @Transactional
    public void markAllAsRead(User currentUser) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        for (Notification notification : notifications) {
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }
}
