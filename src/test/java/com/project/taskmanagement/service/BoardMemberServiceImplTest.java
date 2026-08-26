package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.entity.Invitation;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.InvitationStatus;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.InvitationRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.impl.BoardMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardMemberServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BoardMemberServiceImpl boardMemberService;

    private User currentUser;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(2L).email("member@example.com").displayName("Member").build();
        testBoard = Board.builder().id(10L).name("Test Board").createdBy(1L).visibility(BoardVisibility.PRIVATE).build();
    }

    @Test
    @DisplayName("Story #47: acceptInvitation với người mời mới -> gửi thông báo BOARD_MEMBER_ADDED cho đúng người vừa được thêm")
    void acceptInvitation_NewMember_SendsBoardMemberAddedNotification() {
        Invitation invitation = Invitation.builder()
                .boardId(10L)
                .email("member@example.com")
                .role(Role.MEMBER)
                .token("abc-token")
                .status(InvitationStatus.PENDING)
                .build();

        when(invitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));
        when(boardMemberRepository.existsByBoardIdAndUserId(10L, 2L)).thenReturn(false);
        when(boardRepository.findById(10L)).thenReturn(Optional.of(testBoard));

        boardMemberService.acceptInvitation("abc-token", currentUser);

        verify(boardMemberRepository).save(any(BoardMember.class));
        verify(notificationService, times(1)).createNotification(
                eq(2L), eq(NotificationType.BOARD_MEMBER_ADDED), anyString(), anyString());
    }

    @Test
    @DisplayName("Story #47: acceptInvitation khi đã là thành viên -> không gửi thông báo")
    void acceptInvitation_AlreadyMember_DoesNotSendNotification() {
        Invitation invitation = Invitation.builder()
                .boardId(10L)
                .email("member@example.com")
                .role(Role.MEMBER)
                .token("abc-token")
                .status(InvitationStatus.PENDING)
                .build();

        when(invitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));
        when(boardMemberRepository.existsByBoardIdAndUserId(10L, 2L)).thenReturn(true);

        boardMemberService.acceptInvitation("abc-token", currentUser);

        verify(boardMemberRepository, never()).save(any(BoardMember.class));
        verifyNoInteractions(notificationService);
    }
}
