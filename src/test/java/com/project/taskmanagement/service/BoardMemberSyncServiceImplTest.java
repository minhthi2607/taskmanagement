package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.service.impl.BoardMemberSyncServiceImpl;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardMemberSyncServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private BoardMemberSyncServiceImpl boardMemberSyncService;

    @Test
    @DisplayName("Bug fix: loại người tạo board khỏi Team không được xóa quyền BoardMember của họ trên board đó")
    void removeMemberFromAllGroupBoardsOfTeam_SkipsBoardOwner() {
        Long teamId = 10L;
        Long ownerUserId = 1L;

        Board groupBoard = Board.builder()
                .id(100L)
                .teamId(teamId)
                .name("Sprint Board")
                .visibility(BoardVisibility.GROUP)
                .createdBy(ownerUserId)
                .createdAt(LocalDateTime.now())
                .build();

        when(boardRepository.findByTeamIdOrderByCreatedAtDesc(teamId)).thenReturn(List.of(groupBoard));

        boardMemberSyncService.removeMemberFromAllGroupBoardsOfTeam(teamId, ownerUserId);

        verify(boardMemberRepository, never()).findByBoardIdAndUserId(anyLong(), anyLong());
        verify(boardMemberRepository, never()).delete(any(BoardMember.class));
    }

    @Test
    @DisplayName("Loại thành viên thường (không phải người tạo board) vẫn bị xóa khỏi board GROUP như bình thường")
    void removeMemberFromAllGroupBoardsOfTeam_RemovesNonOwnerMember() {
        Long teamId = 10L;
        Long ownerUserId = 1L;
        Long memberUserId = 2L;

        Board groupBoard = Board.builder()
                .id(100L)
                .teamId(teamId)
                .name("Sprint Board")
                .visibility(BoardVisibility.GROUP)
                .createdBy(ownerUserId)
                .createdAt(LocalDateTime.now())
                .build();

        BoardMember boardMember = BoardMember.builder()
                .boardId(100L)
                .userId(memberUserId)
                .role(Role.MEMBER)
                .build();

        when(boardRepository.findByTeamIdOrderByCreatedAtDesc(teamId)).thenReturn(List.of(groupBoard));
        when(boardMemberRepository.findByBoardIdAndUserId(100L, memberUserId)).thenReturn(Optional.of(boardMember));

        boardMemberSyncService.removeMemberFromAllGroupBoardsOfTeam(teamId, memberUserId);

        verify(boardMemberRepository).delete(boardMember);
    }
}
