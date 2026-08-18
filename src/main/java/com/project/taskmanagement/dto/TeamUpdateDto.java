package com.project.taskmanagement.dto;

import com.project.taskmanagement.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamUpdateDto {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 255, message = "Tên nhóm không quá 255 ký tự")
    private String name;

    @NotBlank(message = "Loại nhóm không được để trống")
    @Size(max = 255, message = "Loại nhóm không quá 255 ký tự")
    private String type;

    @NotNull(message = "Vui lòng chọn quyền riêng tư của nhóm")
    private Visibility visibility;

    private String description;
}
