package com.project.taskmanagement.dto;

import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDueDateDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @Min(value = 0, message = "Số phút nhắc trước không được là số âm")
    private Integer reminderMinutes;
}
