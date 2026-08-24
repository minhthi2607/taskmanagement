package com.project.taskmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardTimeLogCreateDto {

    @NotNull(message = "Số giờ làm việc không được để trống!")
    @DecimalMin(value = "0.01", message = "Số giờ làm việc phải lớn hơn 0!")
    @DecimalMax(value = "999.99", message = "Số giờ làm việc không được vượt quá 999.99 giờ!")
    private BigDecimal hours;

    private String note;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime loggedAt;
}
