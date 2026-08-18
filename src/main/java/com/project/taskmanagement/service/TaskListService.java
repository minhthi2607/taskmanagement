package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.TaskListCreateDto;
import com.project.taskmanagement.dto.TaskListUpdateDto;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;

import java.util.List;

public interface TaskListService {

    /**
     * Story #28: Tạo mới TaskList trong board
     */
    TaskList createTaskList(TaskListCreateDto dto, User currentUser);

    /**
     * Story #29: Đổi tên TaskList
     */
    TaskList updateTaskListName(Long taskListId, String name, User currentUser);

    /**
     * Story #30: Đổi vị trí TaskList (dùng position thưa, bước nhảy 10)
     */
    TaskList updateTaskListPosition(Long taskListId, Integer newPosition, User currentUser);

    /**
     * Story #31: Xóa TaskList (có popup xác nhận)
     */
    void deleteTaskList(Long taskListId, User currentUser);

    /**
     * Lấy tất cả TaskList thuộc Board, sắp xếp theo position tăng dần
     */
    List<TaskList> getTaskListsByBoardId(Long boardId);

    /**
     * Lấy chi tiết TaskList theo ID
     */
    TaskList getTaskListById(Long taskListId);
}
