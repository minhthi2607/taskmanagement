package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.CardTimeLogCreateDto;
import com.project.taskmanagement.entity.CardTimeLog;
import com.project.taskmanagement.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface CardTimeLogService {

    /**
     * Story #55: Ghi nhận nhật ký thời gian (Time Log) cho thẻ công việc
     */
    CardTimeLog addTimeLog(Long cardId, CardTimeLogCreateDto dto, User currentUser);

    /**
     * Story #55: Xóa bản ghi nhật ký thời gian
     */
    void deleteTimeLog(Long timeLogId, User currentUser);

    /**
     * Lấy danh sách nhật ký thời gian của 1 thẻ (mới nhất xếp trên)
     */
    List<CardTimeLog> getTimeLogsByCardId(Long cardId);

    /**
     * Lấy tổng số giờ đã log của 1 thẻ
     */
    BigDecimal getTotalHoursByCardId(Long cardId);

    /**
     * Lấy Map tổng số giờ log theo danh sách ID thẻ (batch query để tránh N+1 Query)
     */
    java.util.Map<Long, BigDecimal> getTotalHoursMapForCards(List<Long> cardIds);
}
