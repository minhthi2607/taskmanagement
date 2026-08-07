package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Invitation;
import com.project.taskmanagement.entity.TeamMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.Role;

import java.util.List;

public interface TeamMemberService {

    /**
     * Mời thành viên vào nhóm bằng Email (Story #16)
     */
    Invitation inviteMember(Long teamId, String email, Role role, User currentUser, String baseUrl);

    /**
     * Chấp nhận lời mời qua Token (Story #16)
     */
    void acceptInvitation(String token, User currentUser);

    /**
     * Loại bỏ thành viên ra khỏi nhóm (Story #17)
     */
    void removeMember(Long teamId, Long targetUserId, User currentUser);

    /**
     * Đổi quyền thành viên trong nhóm (Story #18)
     */
    TeamMember updateMemberRole(Long teamId, Long targetUserId, Role newRole, User currentUser);

    /**
     * Lấy danh sách thành viên thuộc nhóm
     */
    List<TeamMember> getTeamMembers(Long teamId);
}
