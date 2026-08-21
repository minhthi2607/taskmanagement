package com.project.taskmanagement.controller;

import com.project.taskmanagement.config.UserPrincipal;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.service.TaskListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskListControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskListService taskListService;

    @InjectMocks
    private TaskListController taskListController;

    private UserPrincipal testPrincipal;
    private TaskList testTaskList;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .email("dao@example.com")
                .displayName("Dao Test")
                .build();
        testPrincipal = new UserPrincipal(user);

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return testPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(taskListController)
                .setCustomArgumentResolvers(principalResolver)
                .build();

        testTaskList = TaskList.builder()
                .id(100L)
                .boardId(10L)
                .name("Cần làm")
                .position(10)
                .build();
    }

    @Test
    @DisplayName("Bug A Fix: AJAX update position hỗ trợ header XMLHttpRequest")
    void updateTaskListPosition_Ajax_Success() throws Exception {
        when(taskListService.updateTaskListPosition(eq(100L), eq(20), any(User.class)))
                .thenReturn(testTaskList);

        mockMvc.perform(post("/task-list/100/update-position")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .param("position", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.position").value(20));
    }

    @Test
    @DisplayName("Bug B Fix: Form submit update position trả về View Name redirect chứ không trả về ResponseBody String")
    void updateTaskListPosition_Form_Success() throws Exception {
        when(taskListService.getTaskListById(100L)).thenReturn(testTaskList);
        when(taskListService.updateTaskListPosition(eq(100L), eq(20), any(User.class)))
                .thenReturn(testTaskList);

        mockMvc.perform(post("/task-list/100/update-position")
                        .param("position", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board/10"));
    }
}
