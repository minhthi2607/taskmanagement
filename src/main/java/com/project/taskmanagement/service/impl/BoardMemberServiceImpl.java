package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.*;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.InvitationStatus;
import com.project.taskmanagement.enums.NotificationType;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.*;
import com.project.taskmanagement.service.BoardMemberService;
import com.project.taskmanagement.service.EmailService;
import com.project.taskmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardMemberServiceImpl implements BoardMemberService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    private boolean isUserAdminOfBoard(Long boardId, Long userId) {
        return boardMemberRepository.findByBoardIdAndUserId(boardId, userId)
                .map(member -> member.getRole() == Role.ADMIN)
                .orElse(false);
    }

    @Override
    @Transactional
    public Invitation inviteMember(Long boardId, String email, Role role, User currentUser, String baseUrl) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        if (!isUserAdminOfBoard(boardId, currentUser.getId())) {
            throw new SecurityException("Chỉ Quản trị bảng mới có quyền mời thành viên mới!");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng!"));

        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        if (trimmedEmail.isBlank()) {
            throw new IllegalArgumentException("Email người được mời không được để trống!");
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(trimmedEmail);
        if (existingUserOpt.isPresent() && boardMemberRepository.existsByBoardIdAndUserId(boardId, existingUserOpt.get().getId())) {
            throw new IllegalArgumentException("Người dùng với email '" + trimmedEmail + "' đã là thành viên của bảng này!");
        }

        Optional<Invitation> pendingInviteOpt = invitationRepository.findByBoardIdAndEmailAndStatus(
                boardId, trimmedEmail, InvitationStatus.PENDING);

        Invitation invitation;
        String token = UUID.randomUUID().toString();

        if (pendingInviteOpt.isPresent()) {
            invitation = pendingInviteOpt.get();
            invitation.setRole(role != null ? role : Role.MEMBER);
            invitation.setToken(token);
            invitation.setCreatedAt(LocalDateTime.now());
        } else {
            invitation = Invitation.builder()
                    .boardId(boardId)
                    .email(trimmedEmail)
                    .role(role != null ? role : Role.MEMBER)
                    .token(token)
                    .status(InvitationStatus.PENDING)
                    .build();
        }

        Invitation savedInvite = invitationRepository.save(invitation);

        String inviteUrl = baseUrl + "/board/accept-invite?token=" + token;
        emailService.sendBoardInvitationEmail(
                trimmedEmail,
                board.getName(),
                currentUser.getDisplayName(),
                inviteUrl,
                (savedInvite.getRole() == Role.ADMIN) ? "Quản trị bảng" : "Thành viên"
        );

        return savedInvite;
    }

    @Override
    @Transactional
    public void acceptInvitation(String token, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để tham gia bảng!");
        }

        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Mã lời mời không hợp lệ hoặc đã hết hạn!"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Lời mời này đã được chấp nhận hoặc không còn hiệu lực!");
        }

        if (invitation.getBoardId() == null) {
            throw new IllegalArgumentException("Lời mời này không phải là lời mời tham gia bảng!");
        }

        if (!boardMemberRepository.existsByBoardIdAndUserId(invitation.getBoardId(), currentUser.getId())) {
            BoardMember newMember = BoardMember.builder()
                    .boardId(invitation.getBoardId())
                    .userId(currentUser.getId())
                    .role(invitation.getRole())
                    .joinedAt(LocalDateTime.now())
                    .build();
            boardMemberRepository.save(newMember);
            notifyBoardMemberAdded(invitation.getBoardId(), currentUser.getId());
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
    }

    /**
     * Story #47: Thông báo cho người vừa được mời khi họ chấp nhận lời mời tham gia bảng.
     * Không bắn khi tự join (joinBoard) vì không có ai "chủ động thêm" họ.
     * Không được để lỗi gửi thông báo làm hỏng thao tác chấp nhận lời mời chính.
     */
    private void notifyBoardMemberAdded(Long boardId, Long newMemberUserId) {
        try {
            Board board = boardRepository.findById(boardId).orElse(null);
            String boardName = board != null ? board.getName() : "";
            String content = "Bạn đã được thêm vào bảng '" + boardName + "'";
            String link = "/board/" + boardId;
            notificationService.createNotification(newMemberUserId, NotificationType.BOARD_MEMBER_ADDED, content, link);
        } catch (Exception e) {
            log.error("Gửi thông báo BOARD_MEMBER_ADDED thất bại cho boardId={}, userId={}: {}", boardId, newMemberUserId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void removeMember(Long boardId, Long targetUserId, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        if (!isUserAdminOfBoard(boardId, currentUser.getId())) {
            throw new SecurityException("Chỉ Quản trị bảng mới có quyền loại thành viên ra khỏi bảng!");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng!"));

        if (board.getCreatedBy().equals(targetUserId)) {
            throw new SecurityException("Không thể loại bỏ người tạo bảng ra khỏi bảng!");
        }

        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Thành viên không thuộc bảng này!"));

        boardMemberRepository.delete(member);
    }

    @Override
    @Transactional
    public BoardMember updateMemberRole(Long boardId, Long targetUserId, Role newRole, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        if (!isUserAdminOfBoard(boardId, currentUser.getId())) {
            throw new SecurityException("Chỉ Quản trị bảng mới có quyền thay đổi vai trò thành viên!");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng!"));

        if (board.getCreatedBy().equals(targetUserId)) {
            throw new SecurityException("Không thể thay đổi vai trò của người tạo bảng!");
        }

        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(boardId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Thành viên không thuộc bảng này!"));

        member.setRole(newRole != null ? newRole : Role.MEMBER);
        return boardMemberRepository.save(member);
    }

    @Override
    @Transactional
    public BoardMember joinBoard(Long boardId, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Bạn cần đăng nhập để tham gia bảng!");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảng!"));

        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, currentUser.getId())) {
            throw new IllegalArgumentException("Bạn đã là thành viên của bảng này!");
        }

        switch (board.getVisibility()) {
            case GROUP:
                if (!teamMemberRepository.existsByTeamIdAndUserId(board.getTeamId(), currentUser.getId())) {
                    throw new AccessDeniedException("Bạn phải là thành viên nhóm mới có thể tham gia bảng này!");
                }
                break;
            case PUBLIC:
                break; // ai cũng join được
            case PRIVATE:
                throw new AccessDeniedException("Bảng riêng tư — cần được mời trực tiếp qua email!");
        }

        BoardMember newMember = BoardMember.builder()
                .boardId(boardId)
                .userId(currentUser.getId())
                .role(Role.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();
        
        return boardMemberRepository.save(newMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardMember> getBoardMembers(Long boardId) {
        return boardMemberRepository.findByBoardId(boardId);
    }
}
