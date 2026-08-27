package com.project.taskmanagement.scheduler;

import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardMember;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scheduled Job quét & gửi thông báo nhắc việc cho Card đến hạn chót (Story #53 - Mục 14.3 GEMINI.md)
 */
@Component
@RequiredArgsConstructor
public class CardDueReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CardDueReminderScheduler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final CardRepository cardRepository;
    private final TaskListRepository taskListRepository;
    private final NotificationService notificationService;

    /**
     * Chạy định kỳ mỗi 1 phút (60,000 ms) quét các Card có dueDate chưa gửi nhắc nhở.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processCardDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Card> pendingCards = cardRepository.findPendingReminderCards();

        if (pendingCards.isEmpty()) {
            return;
        }

        for (Card card : pendingCards) {
            try {
                LocalDateTime dueDate = card.getDueDate();
                if (dueDate == null) {
                    continue;
                }

                int reminderMinutes = card.getReminderMinutes() != null ? card.getReminderMinutes() : 0;
                LocalDateTime reminderTime = dueDate.minusMinutes(reminderMinutes);

                // Nếu thời điểm hiện tại đã đến hoặc vượt quá mốc cần nhắc việc (now >= reminderTime)
                if (!now.isBefore(reminderTime)) {
                    // Thu thập ID người nhận: Người tạo thẻ + Tất cả thành viên trong thẻ (loại trùng lặp)
                    Set<Long> recipientUserIds = new HashSet<>();
                    if (card.getCreatedBy() != null) {
                        recipientUserIds.add(card.getCreatedBy());
                    }
                    if (card.getMembers() != null) {
                        for (CardMember member : card.getMembers()) {
                            if (member.getUserId() != null) {
                                recipientUserIds.add(member.getUserId());
                            }
                        }
                    }

                    if (!recipientUserIds.isEmpty()) {
                        String formattedDueDate = dueDate.format(DATE_FORMATTER);
                        String content = "Nhắc nhở: Thẻ '" + card.getTitle() + "' đến hạn chót vào lúc " + formattedDueDate;

                        Long boardId = null;
                        if (card.getTaskListId() != null) {
                            boardId = taskListRepository.findById(card.getTaskListId())
                                    .map(TaskList::getBoardId)
                                    .orElse(null);
                        }

                        String link = (boardId != null) ? "/board/" + boardId : "/";

                        for (Long userId : recipientUserIds) {
                            notificationService.createNotification(userId, NotificationType.CARD_DUE_REMINDER, content, link);
                        }
                    }

                    // Cập nhật reminderSentAt xuống DB để không quét lặp
                    card.setReminderSentAt(now);
                    cardRepository.save(card);

                    logger.info("Đã gửi thông báo CARD_DUE_REMINDER cho cardId={} thành công.", card.getId());
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xử lý nhắc nhở hạn chót cho cardId={}: {}", card.getId(), e.getMessage(), e);
            }
        }
    }
}
