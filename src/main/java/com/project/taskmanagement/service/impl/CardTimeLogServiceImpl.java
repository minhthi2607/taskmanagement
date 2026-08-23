package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.CardTimeLogCreateDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardTimeLog;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.repository.CardTimeLogRepository;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.CardTimeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardTimeLogServiceImpl implements CardTimeLogService {

    private final CardTimeLogRepository cardTimeLogRepository;
    private final CardRepository cardRepository;
    private final TaskListRepository taskListRepository;
    private final BoardPermissionService boardPermissionService;

    private Card getCardOrThrow(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ công việc với ID: " + cardId));
    }

    private TaskList getTaskListOrThrow(Long taskListId) {
        return taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh sách công việc với ID: " + taskListId));
    }

    @Override
    @Transactional
    public CardTimeLog addTimeLog(Long cardId, CardTimeLogCreateDto dto, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để ghi nhận thời gian làm việc!");
        }

        Card card = getCardOrThrow(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        if (dto == null || dto.getHours() == null || dto.getHours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số giờ làm việc phải lớn hơn 0!");
        }
        if (dto.getHours().compareTo(new BigDecimal("999.99")) > 0) {
            throw new IllegalArgumentException("Số giờ làm việc không được vượt quá 999.99 giờ!");
        }

        LocalDateTime loggedAt = (dto.getLoggedAt() != null) ? dto.getLoggedAt() : LocalDateTime.now();

        // Convention 6.14: Dual-mapping FK -> set cardId và userId qua field Long
        CardTimeLog timeLog = CardTimeLog.builder()
                .cardId(cardId)
                .userId(currentUser.getId())
                .hours(dto.getHours())
                .note(dto.getNote() != null ? dto.getNote().trim() : null)
                .loggedAt(loggedAt)
                .build();

        return cardTimeLogRepository.save(timeLog);
    }

    @Override
    @Transactional
    public void deleteTimeLog(Long timeLogId, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        CardTimeLog timeLog = cardTimeLogRepository.findById(timeLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký thời gian với ID: " + timeLogId));

        Card card = getCardOrThrow(timeLog.getCardId());
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());

        boolean isOwner = timeLog.getUserId().equals(currentUser.getId());
        boolean isBoardAdmin = boardPermissionService.isBoardAdmin(taskList.getBoardId(), currentUser.getId());
        boolean canEdit = boardPermissionService.canEditBoard(taskList.getBoardId(), currentUser.getId());

        if (!isOwner && !isBoardAdmin && !canEdit) {
            throw new AccessDeniedException("Bạn không có quyền xóa nhật ký thời gian này!");
        }

        cardTimeLogRepository.delete(timeLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardTimeLog> getTimeLogsByCardId(Long cardId) {
        return cardTimeLogRepository.findByCardIdOrderByLoggedAtDescCreatedAtDesc(cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalHoursByCardId(Long cardId) {
        BigDecimal total = cardTimeLogRepository.getTotalHoursByCardId(cardId);
        return (total != null) ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<Long, BigDecimal> getTotalHoursMapForCards(List<Long> cardIds) {
        java.util.Map<Long, BigDecimal> resultMap = new java.util.HashMap<>();
        if (cardIds == null || cardIds.isEmpty()) {
            return resultMap;
        }

        List<Object[]> rawResults = cardTimeLogRepository.getTotalHoursByCardIds(cardIds);
        if (rawResults != null) {
            for (Object[] row : rawResults) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    Long cId = (Long) row[0];
                    BigDecimal total = (BigDecimal) row[1];
                    resultMap.put(cId, total);
                }
            }
        }
        return resultMap;
    }
}
