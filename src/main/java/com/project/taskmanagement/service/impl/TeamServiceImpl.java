package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.TeamCreateDto;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.TeamMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Team createTeam(TeamCreateDto dto, User currentUser) {
        // Nếu người dùng chưa đăng nhập (đang demo/chưa tích hợp Auth đầy đủ),
        // tự động lấy/tạo tài khoản Quản trị viên mặc định để đảm bảo khóa ngoại MySQL không bị lỗi.
        if (currentUser == null || currentUser.getId() == null) {
            currentUser = userRepository.findByEmail("admin@taskmanagement.com")
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email("admin@taskmanagement.com")
                            .displayName("Quản trị viên")
                            .password("$2a$10$7R0w7J8/4mF5X7kQ8V6bE.W7bE7Z7Z7Z7Z7Z7Z7Z7Z7Z7")
                            .build()));
        }

        // 1. Lưu thông tin Nhóm
        Team team = Team.builder()
                .name(dto.getName().trim())
                .type(dto.getType().trim())
                .visibility(dto.getVisibility())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .createdBy(currentUser.getId())
                .build();

        Team savedTeam = teamRepository.save(team);

        // 2. Tự động gán người tạo làm Quản trị nhóm (ADMIN)
        TeamMember adminMember = TeamMember.builder()
                .teamId(savedTeam.getId())
                .userId(currentUser.getId())
                .role(Role.ADMIN)
                .build();

        teamMemberRepository.save(adminMember);

        return savedTeam;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getUserTeamsAlphabetical(Long userId) {
        return teamRepository.findUserTeamsOrderByNameAsc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getAllTeamsAlphabetical() {
        return teamRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm với ID: " + teamId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMemberOfTeam(Long teamId, Long userId) {
        return teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
    }
}
