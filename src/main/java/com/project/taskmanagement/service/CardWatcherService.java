package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.entity.CardWatcher;
import java.util.List;

public interface CardWatcherService {
    
    /**
     * Bật/tắt theo dõi thẻ.
     * Trả về true nếu thẻ đang được theo dõi sau thao tác, false nếu đã hủy theo dõi.
     */
    boolean toggleWatchCard(Long cardId, User currentUser);
    
    /**
     * Kiểm tra xem user có đang theo dõi thẻ hay không.
     */
    boolean isWatching(Long cardId, Long userId);
    
    /**
     * Lấy danh sách những người đang theo dõi thẻ.
     */
    List<CardWatcher> getWatchers(Long cardId);
}
