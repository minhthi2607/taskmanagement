package com.project.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private String email;

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Pattern(
            regexp = "^[\\p{L}\\p{N} ]+$",
            message = "Tên hiển thị không được chứa ký tự đặc biệt"
    )
    private String displayName;

    @Pattern(
            regexp = "^(|0[0-9]{9,10})$",
            message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và gồm 10-11 chữ số)"
    )
    private String phone;

    private String avatarUrl;
}
