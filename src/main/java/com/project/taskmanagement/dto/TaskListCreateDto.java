package com.project.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListCreateDto {

    @NotNull(message = "ID của bảng không được để trống!")
    private Long boardId;

    @NotBlank(message = "Tên danh sách công việc không được để trống!")
    private String name;
}
