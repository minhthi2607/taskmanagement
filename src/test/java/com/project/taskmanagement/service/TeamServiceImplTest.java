package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.TeamUpdateDto;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.enums.Visibility;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team testTeam;

    @BeforeEach
    void setUp() {
        testTeam = Team.builder()
                .id(1L)
                .name("Nhóm Test")
                .type("Kỹ thuật")
                .visibility(Visibility.PRIVATE)
                .createdBy(100L)
                .build();
    }

    @Test
    @DisplayName("isUserAdminOfTeam trả về false khi userId == null (Khách chưa đăng nhập)")
    void isUserAdminOfTeam_WhenUserIdIsNull_ShouldReturnFalse() {
        boolean isAdmin = teamService.isUserAdminOfTeam(1L, null);
        assertFalse(isAdmin, "Người dùng chưa đăng nhập không bao giờ là Admin của nhóm");
    }

    @Test
    @DisplayName("updateTeam ném SecurityException khi currentUser == null (Khách chưa đăng nhập)")
    void updateTeam_WhenCurrentUserIsNull_ShouldThrowSecurityException() {
        TeamUpdateDto dto = TeamUpdateDto.builder()
                .name("Tên Mới")
                .type("Kỹ thuật")
                .visibility(Visibility.PUBLIC)
                .build();

        SecurityException exception = assertThrows(SecurityException.class, () ->
                teamService.updateTeam(1L, dto, null)
        );

        assertEquals("Bạn không có quyền chỉnh sửa nhóm này!", exception.getMessage());
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTeam ném SecurityException khi currentUser == null (Khách chưa đăng nhập)")
    void deleteTeam_WhenCurrentUserIsNull_ShouldThrowSecurityException() {
        SecurityException exception = assertThrows(SecurityException.class, () ->
                teamService.deleteTeam(1L, null)
        );

        assertEquals("Bạn không có quyền xóa nhóm này!", exception.getMessage());
        verify(teamRepository, never()).delete(any());
    }
}
