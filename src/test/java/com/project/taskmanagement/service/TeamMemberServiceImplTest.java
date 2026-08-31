package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.TeamMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.repository.InvitationRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.impl.TeamMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamMemberServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private EmailService emailService;

    @Mock
    private BoardMemberSyncService boardMemberSyncService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TeamMemberServiceImpl teamMemberService;

    private User currentUser;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("admin@example.com").displayName("Admin").build();
        testTeam = Team.builder().id(10L).name("Test Team").createdBy(1L).build();
    }

    @Test
    @DisplayName("removeMember chỉ delegate qua boardMemberSyncService, không tự xóa BoardMember trực tiếp")
    void removeMember_DelegatesToSyncServiceOnly() {
        TeamMember member = TeamMember.builder().teamId(10L).userId(2L).role(Role.MEMBER).build();

        when(teamService.isUserAdminOfTeam(10L, 1L)).thenReturn(true);
        when(teamRepository.findById(10L)).thenReturn(Optional.of(testTeam));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 2L)).thenReturn(Optional.of(member));

        teamMemberService.removeMember(10L, 2L, currentUser);

        verify(teamMemberRepository).delete(member);
        verify(boardMemberSyncService).removeMemberFromAllGroupBoardsOfTeam(10L, 2L);
        verifyNoMoreInteractions(boardMemberSyncService);
    }

    @Test
    @DisplayName("acceptInvitation chỉ delegate qua boardMemberSyncService để đồng bộ board GROUP")
    void acceptInvitation_DelegatesToSyncServiceOnly() {
        com.project.taskmanagement.entity.Invitation invitation = com.project.taskmanagement.entity.Invitation.builder()
                .teamId(10L)
                .email(currentUser.getEmail())
                .role(Role.MEMBER)
                .token("abc-token")
                .status(com.project.taskmanagement.enums.InvitationStatus.PENDING)
                .build();

        when(invitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));
        when(teamMemberRepository.existsByTeamIdAndUserId(10L, 1L)).thenReturn(false);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(testTeam));

        teamMemberService.acceptInvitation("abc-token", currentUser);

        verify(teamMemberRepository).save(any(TeamMember.class));
        verify(boardMemberSyncService).addMemberToAllGroupBoardsOfTeam(10L, 1L);
        verifyNoMoreInteractions(boardMemberSyncService);
        verify(notificationService, times(1)).createNotification(
                eq(1L), eq(NotificationType.TEAM_MEMBER_ADDED), anyString(), anyString());
    }
}
