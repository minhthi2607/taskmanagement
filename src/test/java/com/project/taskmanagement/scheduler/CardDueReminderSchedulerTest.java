package com.project.taskmanagement.scheduler;

import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardMember;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardDueReminderSchedulerTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CardDueReminderScheduler scheduler;

    private TaskList taskList;

    @BeforeEach
    void setUp() {
        taskList = TaskList.builder().id(10L).boardId(100L).name("To Do").position(1).build();
    }

    @Test
    @DisplayName("reminderMinutes null -> mặc định nhắc đúng lúc dueDate, gửi thông báo và set reminderSentAt")
    void processCardDueReminders_reminderMinutesNull_sendsNotificationAtDueDate() {
        Card card = Card.builder()
                .id(1L)
                .taskListId(10L)
                .title("Card A")
                .createdBy(100L)
                .dueDate(LocalDateTime.now().minusSeconds(5))
                .reminderMinutes(null)
                .reminderSentAt(null)
                .build();

        when(cardRepository.findPendingReminderCards()).thenReturn(List.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        scheduler.processCardDueReminders();

        verify(notificationService, times(1))
                .createNotification(eq(100L), eq(NotificationType.CARD_DUE_REMINDER), anyString(), anyString());
        verify(cardRepository, times(1)).save(card);
        assertNotNull(card.getReminderSentAt());
    }

    @Test
    @DisplayName("Card chưa tới mốc nhắc -> không gửi thông báo, không set reminderSentAt")
    void processCardDueReminders_dueDateNotReachedYet_doesNotSend() {
        Card card = Card.builder()
                .id(2L)
                .taskListId(10L)
                .title("Card B")
                .createdBy(100L)
                .dueDate(LocalDateTime.now().plusHours(2))
                .reminderMinutes(null)
                .reminderSentAt(null)
                .build();

        when(cardRepository.findPendingReminderCards()).thenReturn(List.of(card));

        scheduler.processCardDueReminders();

        verify(notificationService, never())
                .createNotification(anyLong(), any(NotificationType.class), anyString(), anyString());
        verify(cardRepository, never()).save(any());
        assertNull(card.getReminderSentAt());
    }

    @Test
    @DisplayName("Card đã gửi rồi (reminderSentAt != null) không còn được query trả về ở lượt quét sau -> không gửi lại")
    void processCardDueReminders_alreadySent_notResentOnNextScan() {
        Card card = Card.builder()
                .id(3L)
                .taskListId(10L)
                .title("Card C")
                .createdBy(100L)
                .dueDate(LocalDateTime.now().minusSeconds(5))
                .reminderMinutes(null)
                .reminderSentAt(null)
                .build();

        // Lượt quét 1: Card khớp điều kiện (reminderSentAt = null). Lượt quét 2: query thật (findPendingReminderCards)
        // đã lọc theo reminderSentAt IS NULL nên không còn trả về Card này -> mô phỏng bằng danh sách rỗng.
        when(cardRepository.findPendingReminderCards())
                .thenReturn(List.of(card))
                .thenReturn(List.of());
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        scheduler.processCardDueReminders();
        scheduler.processCardDueReminders();

        verify(notificationService, times(1))
                .createNotification(eq(100L), eq(NotificationType.CARD_DUE_REMINDER), anyString(), anyString());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    @DisplayName("Người tạo thẻ đồng thời là CardMember -> chỉ nhận đúng 1 thông báo, không trùng")
    void processCardDueReminders_creatorIsAlsoMember_sendsSingleNotification() {
        CardMember selfMember = CardMember.builder().id(500L).cardId(4L).userId(100L).build();
        Card card = Card.builder()
                .id(4L)
                .taskListId(10L)
                .title("Card D")
                .createdBy(100L)
                .dueDate(LocalDateTime.now().minusSeconds(5))
                .reminderMinutes(null)
                .reminderSentAt(null)
                .members(List.of(selfMember))
                .build();

        when(cardRepository.findPendingReminderCards()).thenReturn(List.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        scheduler.processCardDueReminders();

        verify(notificationService, times(1))
                .createNotification(eq(100L), eq(NotificationType.CARD_DUE_REMINDER), anyString(), anyString());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("1 Card ném exception khi xử lý không làm dừng việc xử lý các Card còn lại")
    void processCardDueReminders_oneCardThrows_othersStillProcessed() {
        Card failingCard = Card.builder()
                .id(5L)
                .taskListId(10L)
                .title("Card lỗi")
                .createdBy(900L)
                .dueDate(LocalDateTime.now().minusSeconds(5))
                .reminderMinutes(null)
                .reminderSentAt(null)
                .build();

        Card healthyCard = Card.builder()
                .id(6L)
                .taskListId(10L)
                .title("Card khỏe mạnh")
                .createdBy(901L)
                .dueDate(LocalDateTime.now().minusSeconds(5))
                .reminderMinutes(null)
                .reminderSentAt(null)
                .build();

        when(cardRepository.findPendingReminderCards()).thenReturn(List.of(failingCard, healthyCard));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        doThrow(new RuntimeException("Lỗi giả lập"))
                .when(notificationService)
                .createNotification(eq(900L), any(NotificationType.class), anyString(), anyString());

        scheduler.processCardDueReminders();

        verify(notificationService, times(1))
                .createNotification(eq(901L), eq(NotificationType.CARD_DUE_REMINDER), anyString(), anyString());
        verify(cardRepository, times(1)).save(healthyCard);
        verify(cardRepository, never()).save(failingCard);
        assertNull(failingCard.getReminderSentAt());
        assertNotNull(healthyCard.getReminderSentAt());
    }
}
