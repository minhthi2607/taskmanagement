package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.TeamCreateDto;
import com.project.taskmanagement.dto.TeamUpdateDto;
import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.entity.User;

import java.util.List;

public interface TeamService {

    /**
     * Tạo mới một nhóm và gán người tạo làm Quản trị nhóm (ADMIN)
     */
    Team createTeam(TeamCreateDto dto, User currentUser);

    /**
     * Cập nhật thông tin nhóm và quyền riêng tư của nhóm (Story #12, #13)
     */
    Team updateTeam(Long teamId, TeamUpdateDto dto, User currentUser);

    /**
     * Xóa 1 nhóm (Story #15)
     */
    void deleteTeam(Long teamId, User currentUser);

    /**
     * Lấy danh sách các nhóm mà user tham gia hoặc tự tạo, sắp xếp theo tên (Alphabet)
     */
    List<Team> getUserTeamsAlphabetical(Long userId);

    /**
     * Lấy danh sách tất cả các nhóm (dành cho chế độ demo / guest)
     */
    List<Team> getAllTeamsAlphabetical();

    /**
     * Lấy thông tin chi tiết của 1 nhóm theo ID
     */
    Team getTeamById(Long teamId);

    /**
     * Kiểm tra user có phải là thành viên của nhóm hay không
     */
    boolean isUserMemberOfTeam(Long teamId, Long userId);

    /**
     * Kiểm tra user có phải là Quản trị nhóm (ADMIN) hoặc người tạo nhóm hay không
     */
    boolean isUserAdminOfTeam(Long teamId, Long userId);
}
