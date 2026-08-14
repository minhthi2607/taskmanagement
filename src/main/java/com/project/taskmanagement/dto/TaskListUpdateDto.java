package com.project.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListUpdateDto {

    @NotBlank(message = "Tên danh sách công việc không được để trống!")
    private String name;

    private Integer position;
}
