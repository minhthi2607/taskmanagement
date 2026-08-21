package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.BoardCreateDto;
import com.project.taskmanagement.dto.BoardUpdateDto;
import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.service.impl.BoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @Mock
    private BoardMemberSyncService boardMemberSyncService;

    @Mock
    private com.project.taskmanagement.repository.TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    private User testUser;
    private Team testTeam;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .displayName("Test User")
                .build();

        testTeam = Team.builder()
                .id(10L)
                .name("Test Team")
                .build();

        testBoard = Board.builder()
                .id(100L)
                .teamId(10L)
                .name("Sprint Board")
                .visibility(BoardVisibility.GROUP)
                .createdBy(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Tạo mới board thành công và tự động gán role ADMIN trong BoardMember")
    void createBoard_Success() {
        BoardCreateDto dto = BoardCreateDto.builder()
                .teamId(10L)
                .name("New Board")
                .visibility(BoardVisibility.PUBLIC)
                .build();

        when(teamRepository.findById(10L)).thenReturn(Optional.of(testTeam));
        when(teamService.isUserMemberOfTeam(10L, 1L)).thenReturn(true);
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        Board created = boardService.createBoard(dto, testUser);

        assertNotNull(created);
        verify(boardRepository).save(any(Board.class));
        verify(boardMemberRepository).save(any(BoardMember.class));
    }

    @Test
    @DisplayName("Đổi tên board thành công")
    void updateBoardName_Success() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(testBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        Board updated = boardService.updateBoardName(100L, "New Name", testUser);

        assertNotNull(updated);
        verify(boardPermissionService).checkAdminPermission(100L, testUser);
        verify(boardRepository).save(testBoard);
    }

    @Test
    @DisplayName("Đổi visibility board thành công")
    void updateBoardVisibility_Success() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(testBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        Board updated = boardService.updateBoardVisibility(100L, BoardVisibility.PRIVATE, testUser);

        assertNotNull(updated);
        verify(boardPermissionService).checkAdminPermission(100L, testUser);
        verify(boardRepository).save(testBoard);
    }

    @Test
    @DisplayName("Xóa board thành công")
    void deleteBoard_Success() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(testBoard));

        boardService.deleteBoard(100L, testUser);

        verify(boardPermissionService).checkAdminPermission(100L, testUser);
        verify(boardRepository).delete(testBoard);
    }

    @Test
    @DisplayName("Story #4: Lấy danh sách bảng do user tự tạo group theo Team và sắp xếp Alphabet")
    void getCreatedBoardsGroupedByTeamAlphabetical_Success() {
        Board board1 = Board.builder().id(1L).teamId(10L).name("Beta Board").createdBy(1L).build();
        Board board2 = Board.builder().id(2L).teamId(10L).name("Alpha Board").createdBy(1L).build();

        when(boardRepository.findByCreatedBy(1L)).thenReturn(java.util.List.of(board1, board2));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(testTeam));

        var result = boardService.getCreatedBoardsGroupedByTeamAlphabetical(1L);

        assertEquals(1, result.size());
        assertEquals("Test Team", result.get(0).getTeam().getName());
        assertEquals("Alpha Board", result.get(0).getBoards().get(0).getName());
        assertEquals("Beta Board", result.get(0).getBoards().get(1).getName());
    }

    @Test
    @DisplayName("Story #5: Lấy danh sách bảng user tham gia (ngoại trừ tự tạo) group theo Team và sắp xếp Alphabet")
    void getJoinedBoardsGroupedByTeamAlphabetical_Success() {
        BoardMember bm1 = BoardMember.builder().boardId(200L).userId(1L).role(Role.MEMBER).build();
        Board joinedBoard = Board.builder().id(200L).teamId(10L).name("Joined Board").createdBy(99L).build();

        when(boardMemberRepository.findByUserId(1L)).thenReturn(java.util.List.of(bm1));
        when(boardRepository.findById(200L)).thenReturn(Optional.of(joinedBoard));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(testTeam));

        var result = boardService.getJoinedBoardsGroupedByTeamAlphabetical(1L);

        assertEquals(1, result.size());
        assertEquals("Joined Board", result.get(0).getBoards().get(0).getName());
    }

    @Test
    @DisplayName("Lấy chi tiết board kiểm tra quyền xem thành công")
    void getBoardDetail_Success() {
        when(boardRepository.findById(100L)).thenReturn(Optional.of(testBoard));

        Board board = boardService.getBoardDetail(100L, testUser);

        assertNotNull(board);
        verify(boardPermissionService).checkViewPermission(100L, testUser);
    }
}
