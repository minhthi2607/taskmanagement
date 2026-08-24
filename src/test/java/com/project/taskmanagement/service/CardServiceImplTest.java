package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.CardSearchDto;
import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardAttachment;
import com.project.taskmanagement.entity.CardComment;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.CardAttachmentRepository;
import com.project.taskmanagement.repository.CardCommentRepository;
import com.project.taskmanagement.repository.CardMemberRepository;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.impl.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMemberRepository cardMemberRepository;

    @Mock
    private CardCommentRepository cardCommentRepository;

    @Mock
    private CardAttachmentRepository cardAttachmentRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private User currentUser;
    private User otherUser;
    private TaskList taskList;
    private Card card;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cardService, "uploadDirConfig", "uploads/");

        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        currentUser = User.builder().id(1L).email("user1@example.com").displayName("User One").build();
        otherUser = User.builder().id(2L).email("user2@example.com").displayName("User Two").build();

        taskList = TaskList.builder().id(10L).boardId(100L).name("To Do").position(10).build();
        card = Card.builder().id(50L).taskListId(10L).title("Test Card").createdBy(1L).build();
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

    @Test
    @DisplayName("addCardComment - Thêm bình luận thành công")
    void addCardComment_Success() {
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        doNothing().when(boardPermissionService).checkEditPermission(100L, currentUser);

        cardService.addCardComment(50L, "Đây là bình luận mới", currentUser);

        verify(cardCommentRepository, times(1)).save(any(CardComment.class));
    }

    @Test
    @DisplayName("addCardComment - Thất bại khi nội dung rỗng")
    void addCardComment_EmptyContent_ThrowsException() {
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        assertThrows(IllegalArgumentException.class, () ->
                cardService.addCardComment(50L, "   ", currentUser)
        );
    }

    @Test
    @DisplayName("updateCardComment - Tác giả sửa bình luận thành công")
    void updateCardComment_Owner_Success() {
        CardComment comment = CardComment.builder().id(1000L).cardId(50L).userId(1L).content("Cũ").build();

        when(cardCommentRepository.findById(1000L)).thenReturn(Optional.of(comment));
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        when(cardCommentRepository.save(any(CardComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardComment updated = cardService.updateCardComment(1000L, "Nội dung mới", currentUser);

        assertEquals("Nội dung mới", updated.getContent());
        verify(cardCommentRepository, times(1)).save(comment);
    }

    @Test
    @DisplayName("updateCardComment - Thất bại khi người sửa không phải chính chủ")
    void updateCardComment_NotOwner_ThrowsException() {
        CardComment comment = CardComment.builder().id(1000L).cardId(50L).userId(1L).content("Cũ").build();

        when(cardCommentRepository.findById(1000L)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () ->
                cardService.updateCardComment(1000L, "Nội dung mới", otherUser)
        );
    }

    @Test
    @DisplayName("deleteCardComment - Tác giả xóa bình luận thành công")
    void deleteCardComment_Owner_Success() {
        CardComment comment = CardComment.builder().id(1000L).cardId(50L).userId(1L).content("Nội dung").build();

        when(cardCommentRepository.findById(1000L)).thenReturn(Optional.of(comment));
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        cardService.deleteCardComment(1000L, currentUser);

        verify(cardCommentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("deleteCardComment - Thất bại khi không phải tác giả (kể cả Board Admin)")
    void deleteCardComment_NotOwner_ThrowsException() {
        CardComment comment = CardComment.builder().id(1000L).cardId(50L).userId(2L).content("Nội dung của user 2").build();

        when(cardCommentRepository.findById(1000L)).thenReturn(Optional.of(comment));
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        assertThrows(AccessDeniedException.class, () ->
                cardService.deleteCardComment(1000L, currentUser)
        );
    }

    @Test
    @DisplayName("addAttachment - Đính kèm tệp thành công")
    void addAttachment_Success() {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());

        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));
        when(cardAttachmentRepository.save(any(CardAttachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardAttachment attachment = cardService.addAttachment(50L, mockFile, currentUser);

        assertNotNull(attachment);
        assertEquals(50L, attachment.getCardId());
        assertEquals("test.txt", attachment.getFileName());
        assertEquals(1L, attachment.getUploadedBy());
        assertTrue(attachment.getFileUrl().startsWith("/uploads/attachments/"));
    }

    @Test
    @DisplayName("addAttachment - Thất bại khi file vượt quá 10MB")
    void addAttachment_FileTooLarge_ThrowsException() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.zip", "application/zip", largeContent);

        assertThrows(IllegalArgumentException.class, () ->
                cardService.addAttachment(50L, largeFile, currentUser)
        );
    }

    @Test
    @DisplayName("deleteAttachment - Người tải lên xóa tệp thành công")
    void deleteAttachment_Owner_Success() {
        CardAttachment attachment = CardAttachment.builder().id(200L).cardId(50L).uploadedBy(1L).fileUrl("/uploads/attachments/test.txt").fileName("test.txt").build();

        when(cardAttachmentRepository.findById(200L)).thenReturn(Optional.of(attachment));
        when(cardRepository.findById(50L)).thenReturn(Optional.of(card));
        when(taskListRepository.findById(10L)).thenReturn(Optional.of(taskList));

        cardService.deleteAttachment(200L, currentUser);

        verify(cardAttachmentRepository, times(1)).delete(attachment);
    }
}
