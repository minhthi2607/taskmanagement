package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.CardUpdateDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardAttachment;
import com.project.taskmanagement.entity.CardComment;
import com.project.taskmanagement.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface CardService {

    /**
     * Story #32: Tạo mới Card trong TaskList — chỉ nhận title, vị trí = cuối danh sách (position thưa)
     */
    Card createCard(Long taskListId, String title, User currentUser);

    /**
     * Story #33: Đổi vị trí Card trong cùng TaskList
     */
    void reorderCardInList(Long cardId, Integer newPosition, User currentUser);

    /**
     * Story #34: Di chuyển Card sang TaskList khác (cùng board)
     */
    void moveCardToAnotherList(Long cardId, Long targetTaskListId, Integer newPosition, User currentUser);

    /**
     * Story #35: Sửa description của Card
     */
    Card updateCardDetail(Long cardId, CardUpdateDto dto, User currentUser);

    void addCardMember(Long cardId, Long userId, User currentUser);

    void removeCardMember(Long cardId, Long userId, User currentUser);
    /**
     * Story #53: Cập nhật Hạn chót & Nhắc việc cho Card
     */
    Card updateCardDueDate(Long cardId, java.time.LocalDateTime dueDate, Integer reminderMinutes, User currentUser);

    /**
     * Story #49: Thêm bình luận vào thẻ
     */
    void addCardComment(Long cardId, String content, User currentUser);

    void addCardLabel(Long cardId, Long labelId, User currentUser);

    void removeCardLabel(Long cardId, Long labelId, User currentUser);

    /**
     * Story #38, #39, #40: Tìm kiếm & Lọc Card trong Board qua DTO và JPA Specification (Task B)
     */
    java.util.List<Card> searchCards(com.project.taskmanagement.dto.CardSearchDto searchDto, User currentUser);

    /**
     * Story #50: Chỉnh sửa bình luận của chính mình
     */
    CardComment updateCardComment(Long commentId, String content, User currentUser);

    /**
     * Story #51: Xóa bình luận của chính mình
     */
    void deleteCardComment(Long commentId, User currentUser);

    /**
     * Story #36: Đính kèm tệp vào thẻ
     */
    CardAttachment addAttachment(Long cardId, MultipartFile file, User currentUser);

    /**
     * Story #36: Xóa tệp đính kèm
     */
    void deleteAttachment(Long attachmentId, User currentUser);

    /**
     * Lấy chi tiết Card theo ID
     */
    Card getCardById(Long cardId);
}
