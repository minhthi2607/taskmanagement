package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.CardSearchDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();
    }

    @Test
    @DisplayName("searchCards thành công với DTO chứa các tiêu chí lọc")
    void searchCards_success() {
        CardSearchDto searchDto = CardSearchDto.builder()
                .boardId(100L)
                .keyword("fix")
                .labelIds(List.of(1L, 2L))
                .memberIds(List.of(1L))
                .build();

        Card card1 = Card.builder().id(10L).title("Fix login bug").build();
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(card1));
        doNothing().when(boardPermissionService).checkViewPermission(100L, user);

        List<Card> results = cardService.searchCards(searchDto, user);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Fix login bug", results.get(0).getTitle());
        verify(boardPermissionService, times(1)).checkViewPermission(100L, user);
        verify(cardRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("searchCards báo lỗi khi không truyền boardId")
    void searchCards_missingBoardId() {
        CardSearchDto searchDto = CardSearchDto.builder().build();

        assertThrows(IllegalArgumentException.class, () -> cardService.searchCards(searchDto, user));
    }
}
