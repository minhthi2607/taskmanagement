package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.TaskListCreateDto;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.impl.TaskListServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceImplTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private BoardPermissionService boardPermissionService;

    @InjectMocks
    private TaskListServiceImpl taskListService;

    private User testUser;
    private TaskList testTaskList;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("dao@example.com")
                .displayName("Dao Test")
                .build();

        testTaskList = TaskList.builder()
                .id(100L)
                .boardId(10L)
                .name("Cần làm")
                .position(10)
                .build();
    }

    @Test
    @DisplayName("Story #28: Tạo mới TaskList thành công với position thưa (bước nhảy 10)")
    void createTaskList_Success() {
        TaskListCreateDto dto = TaskListCreateDto.builder()
                .boardId(10L)
                .name("Đang làm")
                .build();

        when(taskListRepository.existsByBoardIdAndName(10L, "Đang làm")).thenReturn(false);
        when(taskListRepository.findTopByBoardIdOrderByPositionDesc(10L)).thenReturn(Optional.of(testTaskList));
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);

        TaskList created = taskListService.createTaskList(dto, testUser);

        assertNotNull(created);
        verify(boardPermissionService).checkEditPermission(10L, testUser);
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    @DisplayName("Tạo mới TaskList thất bại khi tên danh sách trùng lặp")
    void createTaskList_DuplicateName_ThrowsException() {
        TaskListCreateDto dto = TaskListCreateDto.builder()
                .boardId(10L)
                .name("Cần làm")
                .build();

        when(taskListRepository.existsByBoardIdAndName(10L, "Cần làm")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> taskListService.createTaskList(dto, testUser));
    }

    @Test
    @DisplayName("Story #29: Đổi tên TaskList thành công")
    void updateTaskListName_Success() {
        when(taskListRepository.findById(100L)).thenReturn(Optional.of(testTaskList));
        when(taskListRepository.existsByBoardIdAndName(10L, "Đã xong")).thenReturn(false);
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);

        TaskList updated = taskListService.updateTaskListName(100L, "Đã xong", testUser);

        assertNotNull(updated);
        assertEquals("Đã xong", testTaskList.getName());
        verify(boardPermissionService).checkEditPermission(10L, testUser);
        verify(taskListRepository).save(testTaskList);
    }

    @Test
    @DisplayName("Story #30: Đổi vị trí TaskList (position) thành công")
    void updateTaskListPosition_Success() {
        when(taskListRepository.findById(100L)).thenReturn(Optional.of(testTaskList));
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);

        TaskList updated = taskListService.updateTaskListPosition(100L, 25, testUser);

        assertNotNull(updated);
        assertEquals(25, testTaskList.getPosition());
        verify(boardPermissionService).checkEditPermission(10L, testUser);
        verify(taskListRepository).save(testTaskList);
    }

    @Test
    @DisplayName("Story #31: Xóa TaskList thành công")
    void deleteTaskList_Success() {
        when(taskListRepository.findById(100L)).thenReturn(Optional.of(testTaskList));

        taskListService.deleteTaskList(100L, testUser);

        verify(boardPermissionService).checkEditPermission(10L, testUser);
        verify(taskListRepository).delete(testTaskList);
    }
}
