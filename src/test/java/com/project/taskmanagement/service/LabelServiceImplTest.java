package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.Label;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.exception.LabelNotFoundException;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.CardLabelRepository;
import com.project.taskmanagement.repository.LabelRepository;
import com.project.taskmanagement.service.impl.LabelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @Mock
    private CardLabelRepository cardLabelRepository;

    @InjectMocks
    private LabelServiceImpl labelService;

    private User currentUser;
    private Board board;
    private Label label;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("user@example.com").displayName("Test User").build();
        board = Board.builder().id(100L).teamId(10L).name("Board").visibility(BoardVisibility.PRIVATE).createdBy(1L).build();
        label = Label.builder().id(500L).boardId(100L).name("Bug").color("#ff0000").build();
    }

    // ---------- createLabel ----------

    @Test
    @DisplayName("createLabel - Thành công")
    void createLabel_Success() {
        doNothing().when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);
        when(boardRepository.findById(100L)).thenReturn(Optional.of(board));
        when(labelRepository.save(any(Label.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Label result = labelService.createLabel(100L, "Bug", "#ff0000", currentUser);

        assertNotNull(result);
        assertEquals("Bug", result.getName());
        assertEquals("#ff0000", result.getColor());
        assertEquals(100L, result.getBoardId());
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    @DisplayName("createLabel - Thất bại khi tên rỗng/blank")
    void createLabel_BlankName_ThrowsException() {
        doNothing().when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);

        assertThrows(IllegalArgumentException.class, () ->
                labelService.createLabel(100L, "   ", "#ff0000", currentUser)
        );
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    @DisplayName("createLabel - Thất bại khi không đủ quyền")
    void createLabel_NoPermission_ThrowsException() {
        doThrow(new AccessDeniedException("Chỉ thành viên hoặc quản trị bảng mới có quyền thao tác!"))
                .when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);

        assertThrows(AccessDeniedException.class, () ->
                labelService.createLabel(100L, "Bug", "#ff0000", currentUser)
        );
        verify(labelRepository, never()).save(any(Label.class));
    }

    // ---------- updateLabel ----------

    @Test
    @DisplayName("updateLabel - Thành công khi đổi cả tên và màu")
    void updateLabel_Success() {
        when(labelRepository.findById(500L)).thenReturn(Optional.of(label));
        doNothing().when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);
        when(labelRepository.save(any(Label.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Label result = labelService.updateLabel(500L, "Feature", "#00ff00", currentUser);

        assertEquals("Feature", result.getName());
        assertEquals("#00ff00", result.getColor());
        verify(labelRepository, times(1)).save(label);
    }

    @Test
    @DisplayName("updateLabel - Thất bại khi nhãn không tồn tại")
    void updateLabel_LabelNotFound_ThrowsException() {
        when(labelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LabelNotFoundException.class, () ->
                labelService.updateLabel(999L, "Feature", "#00ff00", currentUser)
        );
        verify(boardPermissionService, never()).checkCardMemberOrLabelPermission(any(), any());
    }

    @Test
    @DisplayName("updateLabel - Thất bại khi không đủ quyền")
    void updateLabel_NoPermission_ThrowsException() {
        when(labelRepository.findById(500L)).thenReturn(Optional.of(label));
        doThrow(new AccessDeniedException("Chỉ thành viên hoặc quản trị bảng mới có quyền thao tác!"))
                .when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);

        assertThrows(AccessDeniedException.class, () ->
                labelService.updateLabel(500L, "Feature", "#00ff00", currentUser)
        );
        verify(labelRepository, never()).save(any(Label.class));
    }

    // ---------- deleteLabel ----------

    @Test
    @DisplayName("deleteLabel - Thành công, gọi cardLabelRepository.deleteByLabelId trước khi xóa Label")
    void deleteLabel_Success_DeletesCardLabelsBeforeLabel() {
        when(labelRepository.findById(500L)).thenReturn(Optional.of(label));
        doNothing().when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);

        labelService.deleteLabel(500L, currentUser);

        InOrder inOrder = inOrder(cardLabelRepository, labelRepository);
        inOrder.verify(cardLabelRepository, times(1)).deleteByLabelId(500L);
        inOrder.verify(labelRepository, times(1)).delete(label);
    }

    @Test
    @DisplayName("deleteLabel - Thất bại khi nhãn không tồn tại")
    void deleteLabel_LabelNotFound_ThrowsException() {
        when(labelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LabelNotFoundException.class, () -> labelService.deleteLabel(999L, currentUser));
        verify(cardLabelRepository, never()).deleteByLabelId(any());
    }

    @Test
    @DisplayName("deleteLabel - Thất bại khi không đủ quyền")
    void deleteLabel_NoPermission_ThrowsException() {
        when(labelRepository.findById(500L)).thenReturn(Optional.of(label));
        doThrow(new AccessDeniedException("Chỉ thành viên hoặc quản trị bảng mới có quyền thao tác!"))
                .when(boardPermissionService).checkCardMemberOrLabelPermission(100L, currentUser);

        assertThrows(AccessDeniedException.class, () -> labelService.deleteLabel(500L, currentUser));
        verify(cardLabelRepository, never()).deleteByLabelId(any());
        verify(labelRepository, never()).delete(any());
    }

    // ---------- getLabelsByBoardId ----------

    @Test
    @DisplayName("getLabelsByBoardId - Gọi đúng findByBoardIdOrderByNameAsc")
    void getLabelsByBoardId_UsesOrderedQuery() {
        List<Label> labels = List.of(label);
        when(labelRepository.findByBoardIdOrderByNameAsc(100L)).thenReturn(labels);

        List<Label> result = labelService.getLabelsByBoardId(100L);

        assertEquals(labels, result);
        verify(labelRepository, times(1)).findByBoardIdOrderByNameAsc(100L);
    }
}
