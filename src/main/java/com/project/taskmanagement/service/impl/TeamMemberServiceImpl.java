package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Invitation;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.TeamMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.InvitationStatus;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.InvitationRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.repository.TeamRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.EmailService;
import com.project.taskmanagement.service.TeamMemberService;
import com.project.taskmanagement.service.BoardMemberSyncService;
import com.project.taskmanagement.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final TeamService teamService;
    private final EmailService emailService;
    private final BoardMemberSyncService boardMemberSyncService;

    @Override
    @Transactional
    public Invitation inviteMember(Long teamId, String email, Role role, User currentUser, String baseUrl) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        // 1. Kiểm tra quyền Quản trị nhóm
        if (!teamService.isUserAdminOfTeam(teamId, currentUser.getId())) {
            throw new SecurityException("Chỉ Quản trị nhóm mới có quyền mời thành viên mới!");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm!"));

        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        if (trimmedEmail.isBlank()) {
            throw new IllegalArgumentException("Email người được mời không được để trống!");
        }

        // 2. Kiểm tra xem user này đã là thành viên nhóm chưa
        Optional<User> existingUserOpt = userRepository.findByEmail(trimmedEmail);
        if (existingUserOpt.isPresent() && teamMemberRepository.existsByTeamIdAndUserId(teamId, existingUserOpt.get().getId())) {
            throw new IllegalArgumentException("Người dùng với email '" + trimmedEmail + "' đã là thành viên của nhóm này!");
        }

        // 3. Kiểm tra xem đã có lời mời PENDING nào cho email này trong nhóm chưa
        Optional<Invitation> pendingInviteOpt = invitationRepository.findByTeamIdAndEmailAndStatus(
                teamId, trimmedEmail, InvitationStatus.PENDING);

        Invitation invitation;
        String token = UUID.randomUUID().toString();

        if (pendingInviteOpt.isPresent()) {
            // Đã có lời mời PENDING cũ -> Cập nhật vai trò mới & Token mới để gửi lại email
            invitation = pendingInviteOpt.get();
            invitation.setRole(role != null ? role : Role.MEMBER);
            invitation.setToken(token);
            invitation.setCreatedAt(LocalDateTime.now());
        } else {
            // Chưa có -> Tạo mới bản ghi Invitation PENDING
            invitation = Invitation.builder()
                    .teamId(teamId)
                    .email(trimmedEmail)
                    .role(role != null ? role : Role.MEMBER)
                    .token(token)
                    .status(InvitationStatus.PENDING)
                    .build();
        }

        Invitation savedInvite = invitationRepository.save(invitation);

        // 4. Mọi trường hợp đều gửi email mời chứa link chấp nhận lời mời
        String inviteUrl = baseUrl + "/team/accept-invite?token=" + token;
        emailService.sendTeamInvitationEmail(
                trimmedEmail,
                team.getName(),
                currentUser.getDisplayName(),
                inviteUrl,
                (savedInvite.getRole() == Role.ADMIN) ? "Quản trị nhóm" : "Thành viên"
        );

        return savedInvite;
    }

    @Override
    @Transactional
    public void acceptInvitation(String token, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để tham gia nhóm!");
        }

        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Mã lời mời không hợp lệ hoặc đã hết hạn!"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời này đã được chấp nhận hoặc không còn hiệu lực!");
        }

        // Kiểm tra xem đã là thành viên nhóm chưa
        if (!teamMemberRepository.existsByTeamIdAndUserId(invitation.getTeamId(), currentUser.getId())) {
            TeamMember newMember = TeamMember.builder()
                    .teamId(invitation.getTeamId())
                    .userId(currentUser.getId())
                    .role(invitation.getRole())
                    .joinedAt(LocalDateTime.now())
                    .build();
            teamMemberRepository.save(newMember);
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        if (invitation.getTeamId() != null) {
            boardMemberSyncService.addMemberToAllGroupBoardsOfTeam(invitation.getTeamId(), currentUser.getId());
        }
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long targetUserId, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        // 1. Kiểm tra quyền Quản trị nhóm
        if (!teamService.isUserAdminOfTeam(teamId, currentUser.getId())) {
            throw new SecurityException("Chỉ Quản trị nhóm mới có quyền loại thành viên ra khỏi nhóm!");
        }

        // 2. Không thể loại bỏ Chủ sở hữu (người tạo nhóm)
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm!"));

        if (team.getCreatedBy().equals(targetUserId)) {
            throw new SecurityException("Không thể loại bỏ Chủ sở hữu (người tạo nhóm) ra khỏi nhóm!");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Thành viên không thuộc nhóm này!"));

        teamMemberRepository.delete(member);
        boardMemberSyncService.removeMemberFromAllGroupBoardsOfTeam(teamId, targetUserId);
    }

    @Override
    @Transactional
    public TeamMember updateMemberRole(Long teamId, Long targetUserId, Role newRole, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        // 1. Kiểm tra quyền Quản trị nhóm
        if (!teamService.isUserAdminOfTeam(teamId, currentUser.getId())) {
            throw new SecurityException("Chỉ Quản trị nhóm mới có quyền thay đổi vai trò thành viên!");
        }

        // 2. Không thể thay đổi vai trò của Chủ sở hữu
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm!"));

        if (team.getCreatedBy().equals(targetUserId)) {
            throw new SecurityException("Không thể thay đổi vai trò của Chủ sở hữu nhóm!");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Thành viên không thuộc nhóm này!"));

        member.setRole(newRole != null ? newRole : Role.MEMBER);
        return teamMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMember> getTeamMembers(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId);
    }
}
