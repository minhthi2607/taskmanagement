package com.project.taskmanagement.service;

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
import com.project.taskmanagement.service.impl.CardTimeLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardTimeLogServiceImplTest {

    @Mock
    private CardTimeLogRepository cardTimeLogRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @InjectMocks
    private CardTimeLogServiceImpl cardTimeLogService;

    private User user;
    private Card card;
    private TaskList taskList;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        taskList = TaskList.builder()
                .id(10L)
                .boardId(100L)
                .name("To Do")
                .build();

        card = Card.builder()
                .id(50L)
                .taskListId(10L)
                .title("Fix Login Bug")
                .build();
    }

    @Test
    @DisplayName("Ghi nhận time log thành công khi có quyền chỉnh sửa Board")
    void addTimeLog_success() {
        CardTimeLogCreateDto dto = CardTimeLogCreateDto.builder()
                .hours(new BigDecimal("1.50"))
                .note("Sửa lỗi BCrypt password encoder")
                .loggedAt(LocalDateTime.now())
                .build();

        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        doNothing().when(boardPermissionService).checkEditPermission(100L, user);
        when(cardTimeLogRepository.save(any(CardTimeLog.class))).thenAnswer(invocation -> {
            CardTimeLog saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CardTimeLog result = cardTimeLogService.addTimeLog(50L, dto, user);

        assertNotNull(result);
        assertEquals(50L, result.getCardId());
        assertEquals(1L, result.getUserId());
        assertEquals(new BigDecimal("1.50"), result.getHours());
        assertEquals("Sửa lỗi BCrypt password encoder", result.getNote());
        verify(cardTimeLogRepository, times(1)).save(any(CardTimeLog.class));
    }

    @Test
    @DisplayName(" Báo lỗi khi số giờ log <= 0 hoặc null")
    void addTimeLog_invalidHours() {
        CardTimeLogCreateDto dto = CardTimeLogCreateDto.builder()
                .hours(BigDecimal.ZERO)
                .build();

        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        doNothing().when(boardPermissionService).checkEditPermission(100L, user);

        assertThrows(IllegalArgumentException.class, () -> cardTimeLogService.addTimeLog(50L, dto, user));
    }

    @Test
    @DisplayName(" Báo lỗi khi người dùng chưa đăng nhập")
    void addTimeLog_unauthenticated() {
        CardTimeLogCreateDto dto = CardTimeLogCreateDto.builder()
                .hours(new BigDecimal("2.0"))
                .build();

        assertThrows(AccessDeniedException.class, () -> cardTimeLogService.addTimeLog(50L, dto, null));
    }

    @Test
    @DisplayName(" Chủ nhân bản ghi time log có thể xóa log của mình")
    void deleteTimeLog_success_owner() {
        CardTimeLog timeLog = CardTimeLog.builder()
                .id(5L)
                .cardId(50L)
                .userId(1L)
                .hours(new BigDecimal("1.0"))
                .build();

        when(cardTimeLogRepository.findById(5L)).thenReturn(Optional.of(timeLog));
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        cardTimeLogService.deleteTimeLog(5L, user);

        verify(cardTimeLogRepository, times(1)).delete(timeLog);
    }

    @Test
    @DisplayName("Admin của Board có thể xóa time log của người khác")
    void deleteTimeLog_success_boardAdmin() {
        User adminUser = User.builder().id(99L).displayName("Admin").build();
        CardTimeLog timeLog = CardTimeLog.builder()
                .id(5L)
                .cardId(50L)
                .userId(1L)
                .hours(new BigDecimal("1.0"))
                .build();

        when(cardTimeLogRepository.findById(5L)).thenReturn(Optional.of(timeLog));
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        when(boardPermissionService.isBoardAdmin(100L, 99L)).thenReturn(true);

        cardTimeLogService.deleteTimeLog(5L, adminUser);

        verify(cardTimeLogRepository, times(1)).delete(timeLog);
    }

    @Test
    @DisplayName("Tính tổng số giờ time log của Card")
    void getTotalHoursByCardId() {
        when(cardTimeLogRepository.getTotalHoursByCardId(50L)).thenReturn(new BigDecimal("3.50"));

        BigDecimal total = cardTimeLogService.getTotalHoursByCardId(50L);

        assertEquals(new BigDecimal("3.50"), total);
    }

    @Test
    @DisplayName("Batch load total hours map bằng 1 SQL Query duy nhất")
    void getTotalHoursMapForCards() {
        Object[] row1 = new Object[]{50L, new BigDecimal("4.50")};
        Object[] row2 = new Object[]{51L, new BigDecimal("2.00")};
        when(cardTimeLogRepository.getTotalHoursByCardIds(List.of(50L, 51L))).thenReturn(List.of(row1, row2));

        java.util.Map<Long, BigDecimal> map = cardTimeLogService.getTotalHoursMapForCards(List.of(50L, 51L));

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals(new BigDecimal("4.50"), map.get(50L));
        assertEquals(new BigDecimal("2.00"), map.get(51L));
    }
}
