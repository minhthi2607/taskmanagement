package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.service.impl.BoardPermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardPermissionServiceImplTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private BoardPermissionServiceImpl boardPermissionService;

    private User user;
    private Board privateBoard;
    private Board groupBoard;
    private Board publicBoard;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").displayName("Test User").build();

        privateBoard = Board.builder().id(100L).teamId(10L).name("Private Board").visibility(BoardVisibility.PRIVATE).createdBy(1L).build();
        groupBoard = Board.builder().id(200L).teamId(20L).name("Group Board").visibility(BoardVisibility.GROUP).createdBy(1L).build();
        publicBoard = Board.builder().id(300L).teamId(30L).name("Public Board").visibility(BoardVisibility.PUBLIC).createdBy(1L).build();
    }

    // ---------- checkAdminPermission ----------

    @Test
    @DisplayName("checkAdminPermission - Thành công khi user là ADMIN của bảng")
    void checkAdminPermission_UserIsAdmin_Success() {
        when(boardRepository.existsById(100L)).thenReturn(true);
        when(boardMemberRepository.existsByBoardIdAndUserIdAndRole(100L, 1L, Role.ADMIN)).thenReturn(true);

        assertDoesNotThrow(() -> boardPermissionService.checkAdminPermission(100L, user));
    }

    @Test
    @DisplayName("checkAdminPermission - Thất bại khi user không phải ADMIN")
    void checkAdminPermission_UserNotAdmin_ThrowsException() {
        when(boardRepository.existsById(100L)).thenReturn(true);
        when(boardMemberRepository.existsByBoardIdAndUserIdAndRole(100L, 1L, Role.ADMIN)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkAdminPermission(100L, user));
    }

    @Test
    @DisplayName("checkAdminPermission - Thất bại khi user chưa đăng nhập")
    void checkAdminPermission_UserNull_ThrowsException() {
        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkAdminPermission(100L, null));
    }

    // ---------- isBoardAdmin ----------

    @Test
    @DisplayName("isBoardAdmin - Trả về true khi user là ADMIN")
    void isBoardAdmin_True() {
        when(boardMemberRepository.existsByBoardIdAndUserIdAndRole(100L, 1L, Role.ADMIN)).thenReturn(true);

        assertTrue(boardPermissionService.isBoardAdmin(100L, 1L));
    }

    @Test
    @DisplayName("isBoardAdmin - Trả về false khi user không phải ADMIN")
    void isBoardAdmin_False() {
        when(boardMemberRepository.existsByBoardIdAndUserIdAndRole(100L, 1L, Role.ADMIN)).thenReturn(false);

        assertFalse(boardPermissionService.isBoardAdmin(100L, 1L));
    }

    @Test
    @DisplayName("isBoardAdmin - Trả về false khi boardId hoặc userId null")
    void isBoardAdmin_NullParams_False() {
        assertFalse(boardPermissionService.isBoardAdmin(null, 1L));
        assertFalse(boardPermissionService.isBoardAdmin(100L, null));
    }

    // ---------- isBoardMember ----------

    @Test
    @DisplayName("isBoardMember - Trả về true khi user là thành viên bảng")
    void isBoardMember_True() {
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(true);

        assertTrue(boardPermissionService.isBoardMember(100L, 1L));
    }

    @Test
    @DisplayName("isBoardMember - Trả về false khi user không phải thành viên bảng")
    void isBoardMember_False() {
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(false);

        assertFalse(boardPermissionService.isBoardMember(100L, 1L));
    }

    // ---------- checkViewPermission ----------

    @Test
    @DisplayName("checkViewPermission - Board PUBLIC luôn xem được kể cả không đăng nhập")
    void checkViewPermission_Public_Success() {
        when(boardRepository.findById(300L)).thenReturn(Optional.of(publicBoard));

        assertDoesNotThrow(() -> boardPermissionService.checkViewPermission(300L, null));
    }

    @Test
    @DisplayName("checkViewPermission - Board PRIVATE, user là BoardMember -> thành công")
    void checkViewPermission_Private_BoardMember_Success() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(privateBoard));
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> boardPermissionService.checkViewPermission(100L, user));
    }

    @Test
    @DisplayName("checkViewPermission - Board PRIVATE, user không phải BoardMember -> ném AccessDeniedException")
    void checkViewPermission_Private_NotBoardMember_ThrowsException() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(privateBoard));
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkViewPermission(100L, user));
    }

    @Test
    @DisplayName("checkViewPermission - Không tìm thấy bảng -> ném ResourceNotFoundException")
    void checkViewPermission_BoardNotFound_ThrowsException() {
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> boardPermissionService.checkViewPermission(999L, user));
    }

    // ---------- checkEditPermission ----------

    @Test
    @DisplayName("checkEditPermission - Thành công khi user là thành viên bảng")
    void checkEditPermission_Success() {
        when(boardRepository.existsById(100L)).thenReturn(true);
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> boardPermissionService.checkEditPermission(100L, user));
    }

    @Test
    @DisplayName("checkEditPermission - Thất bại khi user không phải thành viên bảng")
    void checkEditPermission_NotMember_ThrowsException() {
        when(boardRepository.existsById(100L)).thenReturn(true);
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkEditPermission(100L, user));
    }

    // ---------- canEditBoard ----------

    @Test
    @DisplayName("canEditBoard - Trả về true khi user là thành viên bảng")
    void canEditBoard_True() {
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(true);

        assertTrue(boardPermissionService.canEditBoard(100L, 1L));
    }

    @Test
    @DisplayName("canEditBoard - Trả về false khi boardId hoặc userId null")
    void canEditBoard_NullParams_False() {
        assertFalse(boardPermissionService.canEditBoard(null, 1L));
        assertFalse(boardPermissionService.canEditBoard(100L, null));
    }

    // ---------- checkCardMemberOrLabelPermission ----------

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Board PRIVATE, user LÀ BoardMember -> không ném exception")
    void checkCardMemberOrLabelPermission_Private_IsBoardMember_Success() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(privateBoard));
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> boardPermissionService.checkCardMemberOrLabelPermission(100L, user));
        verify(teamMemberRepository, never()).existsByTeamIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Board PRIVATE, user KHÔNG phải BoardMember -> ném AccessDeniedException")
    void checkCardMemberOrLabelPermission_Private_NotBoardMember_ThrowsException() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(privateBoard));
        when(boardMemberRepository.existsByBoardIdAndUserId(100L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkCardMemberOrLabelPermission(100L, user));
    }

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Board GROUP, user LÀ TeamMember -> không ném exception")
    void checkCardMemberOrLabelPermission_Group_IsTeamMember_Success() {
        when(boardRepository.findById(200L)).thenReturn(Optional.of(groupBoard));
        when(teamMemberRepository.existsByTeamIdAndUserId(20L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> boardPermissionService.checkCardMemberOrLabelPermission(200L, user));
    }

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Board GROUP, user LÀ BoardMember nhưng KHÔNG phải TeamMember -> ném AccessDeniedException (lỗ hổng bảo mật đã fix)")
    void checkCardMemberOrLabelPermission_Group_BoardMemberButNotTeamMember_ThrowsException() {
        when(boardRepository.findById(200L)).thenReturn(Optional.of(groupBoard));
        when(teamMemberRepository.existsByTeamIdAndUserId(20L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkCardMemberOrLabelPermission(200L, user));
        // Đảm bảo việc là BoardMember không được dùng để "lách" kiểm tra TeamMember cho board GROUP
        verify(boardMemberRepository, never()).existsByBoardIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Board PUBLIC, user LÀ TeamMember -> không ném exception")
    void checkCardMemberOrLabelPermission_Public_IsTeamMember_Success() {
        when(boardRepository.findById(300L)).thenReturn(Optional.of(publicBoard));
        when(teamMemberRepository.existsByTeamIdAndUserId(30L, 1L)).thenReturn(true);

        assertDoesNotThrow(() -> boardPermissionService.checkCardMemberOrLabelPermission(300L, user));
    }

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Board PUBLIC, user LÀ BoardMember nhưng KHÔNG phải TeamMember -> ném AccessDeniedException")
    void checkCardMemberOrLabelPermission_Public_BoardMemberButNotTeamMember_ThrowsException() {
        when(boardRepository.findById(300L)).thenReturn(Optional.of(publicBoard));
        when(teamMemberRepository.existsByTeamIdAndUserId(30L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> boardPermissionService.checkCardMemberOrLabelPermission(300L, user));
    }

    @Test
    @DisplayName("checkCardMemberOrLabelPermission - Không tìm thấy bảng -> ném ResourceNotFoundException")
    void checkCardMemberOrLabelPermission_BoardNotFound_ThrowsException() {
        when(boardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> boardPermissionService.checkCardMemberOrLabelPermission(999L, user));
    }
}
