package com.project.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO đóng gói các tiêu chí tìm kiếm/lọc Card trong Board (Task #38, #39, #40)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardSearchDto {

    /**
     * ID của Board cần tìm kiếm (bắt buộc, không được để lọt Card của Board khác)
     */
    private Long boardId;

    /**
     * Story #38: Từ khóa tiêu đề thẻ (tìm gần đúng LIKE, case-insensitive)
     */
    private String keyword;

    /**
     * Story #39: Danh sách ID các nhãn được chọn (IN clause)
     */
    private List<Long> labelIds;

    /**
     * Story #40: Danh sách ID các thành viên được chọn (IN clause)
     */
    private List<Long> memberIds;
}
