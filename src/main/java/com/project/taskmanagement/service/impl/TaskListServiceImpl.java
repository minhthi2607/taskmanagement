package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.TaskListCreateDto;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.TaskListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;
    private final BoardPermissionService boardPermissionService;

    /**
     * Story #28: Tạo mới TaskList trong board
     */
    @Override
    @Transactional
    public TaskList createTaskList(TaskListCreateDto dto, User currentUser) {
        if (dto == null || dto.getBoardId() == null) {
            throw new IllegalArgumentException("Thông tin bảng công việc không hợp lệ!");
        }

        Long boardId = dto.getBoardId();
        boardPermissionService.checkEditPermission(boardId, currentUser);

        String name = dto.getName() != null ? dto.getName().trim() : "";
        if (name.isBlank()) {
            throw new IllegalArgumentException("Tên danh sách công việc không được để trống!");
        }

        if (name.length() > 255) {
            throw new IllegalArgumentException("Tên danh sách công việc không được vượt quá 255 ký tự!");
        }

        if (taskListRepository.existsByBoardIdAndName(boardId, name)) {
            throw new IllegalArgumentException("Danh sách công việc với tên '" + name + "' đã tồn tại trong bảng này!");
        }

        // Tính position thưa (bước nhảy 10): max(position hiện có) + 10
        Integer nextPosition = taskListRepository.findTopByBoardIdOrderByPositionDesc(boardId)
                .map(lastList -> (lastList.getPosition() != null ? lastList.getPosition() : 0) + 10)
                .orElse(10);

        TaskList taskList = TaskList.builder()
                .boardId(boardId)
                .name(name)
                .position(nextPosition)
                .createdAt(LocalDateTime.now())
                .build();

        return taskListRepository.save(taskList);
    }

    /**
     * Story #29: Đổi tên TaskList
     */
    @Override
    @Transactional
    public TaskList updateTaskListName(Long taskListId, String name, User currentUser) {
        TaskList taskList = getTaskListById(taskListId);
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        String newName = name != null ? name.trim() : "";
        if (newName.isBlank()) {
            throw new IllegalArgumentException("Tên danh sách công việc không được để trống!");
        }

        if (newName.length() > 255) {
            throw new IllegalArgumentException("Tên danh sách công việc không được vượt quá 255 ký tự!");
        }

        if (!taskList.getName().equalsIgnoreCase(newName) && taskListRepository.existsByBoardIdAndName(taskList.getBoardId(), newName)) {
            throw new IllegalArgumentException("Danh sách công việc với tên '" + newName + "' đã tồn tại trong bảng này!");
        }

        taskList.setName(newName);
        return taskListRepository.save(taskList);
    }

    /**
     * Story #30: Đổi vị trí TaskList (dùng position thưa)
     */
    @Override
    @Transactional
    public TaskList updateTaskListPosition(Long taskListId, Integer newPosition, User currentUser) {
        TaskList taskList = getTaskListById(taskListId);
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        if (newPosition == null || newPosition < 0) {
            throw new IllegalArgumentException("Vị trí của danh sách công việc không hợp lệ!");
        }

        taskList.setPosition(newPosition);
        return taskListRepository.save(taskList);
    }

    /**
     * Story #31: Xóa TaskList (có popup xác nhận ở UI)
     */
    @Override
    @Transactional
    public void deleteTaskList(Long taskListId, User currentUser) {
        TaskList taskList = getTaskListById(taskListId);
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        taskListRepository.delete(taskList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskList> getTaskListsByBoardId(Long boardId) {
        return taskListRepository.findByBoardIdOrderByPositionAsc(boardId);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskList getTaskListById(Long taskListId) {
        return taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh sách công việc với ID: " + taskListId));
    }
}
