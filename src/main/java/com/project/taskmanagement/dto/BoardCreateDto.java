package com.project.taskmanagement.dto;

import com.project.taskmanagement.enums.BoardVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCreateDto {

    @NotNull(message = "ID nhóm không được để trống!")
    private Long teamId;

    @NotBlank(message = "Tên bảng không được để trống!")
    @Size(max = 100, message = "Tên bảng không được quá 100 ký tự!")
    private String name;

    @NotNull(message = "Quyền truy cập bảng không được để trống!")
    private BoardVisibility visibility;
}
