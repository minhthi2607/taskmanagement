package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.CardUpdateDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.User;

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

    void addCardComment(Long cardId, String content, User currentUser);

    /**
     * Lấy chi tiết Card theo ID
     */
    Card getCardById(Long cardId);
}
