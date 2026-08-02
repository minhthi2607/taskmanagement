package com.project.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDto {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Pattern(regexp = "^[\\p{L}\\p{N} ]*$", message = "Tên hiển thị không được chứa ký tự đặc biệt")
    private String displayName;

    @Pattern(regexp = "^(|0[0-9]{9,10})$", message = "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và gồm 10-11 chữ số)")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 32, message = "Mật khẩu phải từ 6 đến 32 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
