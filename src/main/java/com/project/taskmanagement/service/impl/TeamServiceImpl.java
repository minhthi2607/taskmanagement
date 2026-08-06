package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.TeamCreateDto;
import com.project.taskmanagement.dto.TeamUpdateDto;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.TeamMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.enums.Visibility;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.TeamService;
import com.project.taskmanagement.service.EmailService;
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
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException("Bạn cần đăng nhập để thực hiện thao tác này.");
        }

        // 1. Lưu thông tin Nhóm
        Team team = Team.builder()
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .type(dto.getType() != null ? dto.getType().trim() : null)
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
    @Transactional
    public Team updateTeam(Long teamId, TeamUpdateDto dto, User currentUser) {
        Long userId = (currentUser != null) ? currentUser.getId() : null;

        // 1. Kiểm tra quyền Quản trị nhóm (Luôn kiểm tra kể cả khi chưa đăng nhập)
        if (!isUserAdminOfTeam(teamId, userId)) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa nhóm này!");
        }

        // 2. Lấy đối tượng Team và cập nhật dữ liệu
        Team team = getTeamById(teamId);
        team.setName(dto.getName().trim());
        team.setType(dto.getType().trim());
        team.setVisibility(dto.getVisibility());
        team.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        return teamRepository.save(team);
    }

    private final EmailService emailService;

    @Override
    @Transactional
    public void deleteTeam(Long teamId, User currentUser) {
        Long userId = (currentUser != null) ? currentUser.getId() : null;

        // 1. Kiểm tra quyền Quản trị nhóm (Luôn kiểm tra kể cả khi chưa đăng nhập)
        if (!isUserAdminOfTeam(teamId, userId)) {
            throw new SecurityException("Bạn không có quyền xóa nhóm này!");
        }

        // 2. Lấy đối tượng Team và danh sách email thành viên trước khi xóa
        Team team = getTeamById(teamId);
        String teamName = team.getName();

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        List<String> memberEmails = members.stream()
                .filter(m -> m.getUser() != null && m.getUser().getEmail() != null)
                .map(m -> m.getUser().getEmail())
                .distinct()
                .toList();

        // 3. Xóa nhóm
        teamRepository.delete(team);

        // 4. Gửi thông báo đến các thành viên nhóm (Story #15)
        String deleterName = (currentUser != null) ? currentUser.getDisplayName() : "Quản trị viên";
        emailService.sendTeamDeletionNotification(teamName, memberEmails, deleterName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getUserTeamsAlphabetical(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return teamRepository.findUserTeamsOrderByNameAsc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getAllTeamsAlphabetical() {
        return teamRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getPublicTeamsAlphabetical() {
        return teamRepository.findByVisibilityOrderByNameAsc(Visibility.PUBLIC);
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
        if (teamId == null || userId == null) {
            return false;
        }
        return teamMemberRepository.existsByTeamIdAndUserId(teamId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserAdminOfTeam(Long teamId, Long userId) {
        if (userId == null)
            return false; // Chưa đăng nhập -> Luôn KHÔNG CÓ QUYỀN Admin
        Team team = getTeamById(teamId);
        if (team.getCreatedBy().equals(userId)) {
            return true;
        }
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .map(member -> member.getRole() == Role.ADMIN)
                .orElse(false);
    }
}
