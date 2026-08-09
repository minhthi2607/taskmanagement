package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.entity.Invitation;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.Role;

import java.util.List;

public interface BoardMemberService {

    Invitation inviteMember(Long boardId, String email, Role role, User currentUser, String baseUrl);

    void acceptInvitation(String token, User currentUser);

    void removeMember(Long boardId, Long targetUserId, User currentUser);

    BoardMember updateMemberRole(Long boardId, Long targetUserId, Role newRole, User currentUser);

    BoardMember joinBoard(Long boardId, User currentUser);

    List<BoardMember> getBoardMembers(Long boardId);
}
