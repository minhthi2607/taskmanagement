package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.TeamCreateDto;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.User;

import java.util.List;

public interface TeamService {

    /**
     * Tạo mới một nhóm và gán người tạo làm Quản trị nhóm (ADMIN)
     */
    Team createTeam(TeamCreateDto dto, User currentUser);

    /**
     * Lấy danh sách các nhóm mà user tham gia hoặc tự tạo, sắp xếp theo tên (Alphabet)
     */
    List<Team> getUserTeamsAlphabetical(Long userId);

    /**
     * Lấy danh sách tất cả các nhóm (dành cho chế độ demo / guest)
     */
    List<Team> getAllTeamsAlphabetical();

    /**
     * Lấy danh sách các nhóm Công khai (PUBLIC), sắp xếp Alphabet (dành cho chế độ guest)
     */
    List<Team> getPublicTeamsAlphabetical();

    /**
     * Lấy thông tin chi tiết của 1 nhóm theo ID
     */
    Team getTeamById(Long teamId);

    /**
     * Kiểm tra user có phải là thành viên của nhóm hay không
     */
    boolean isUserMemberOfTeam(Long teamId, Long userId);

    /**
     * Kiểm tra user có phải là Quản trị nhóm (ADMIN) của nhóm hay không
     */
    boolean isUserAdminOfTeam(Long teamId, Long userId);
}
