package com.project.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCreateDto {

    @NotBlank(message = "Tiêu đề thẻ không được để trống!")
    private String title;
}
