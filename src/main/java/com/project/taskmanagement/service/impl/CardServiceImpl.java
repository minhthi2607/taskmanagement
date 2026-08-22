package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.CardUpdateDto;
import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardComment;
import com.project.taskmanagement.entity.CardLabel;
import com.project.taskmanagement.entity.CardMember;
import com.project.taskmanagement.entity.TaskList;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.ResourceNotFoundException;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.CardCommentRepository;
import com.project.taskmanagement.repository.CardMemberRepository;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.repository.LabelRepository;
import com.project.taskmanagement.repository.CardLabelRepository;
import com.project.taskmanagement.repository.TaskListRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMemberRepository cardMemberRepository;
    private final CardCommentRepository cardCommentRepository;
    private final TaskListRepository taskListRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardPermissionService boardPermissionService;
    private final LabelRepository labelRepository;
    private final CardLabelRepository cardLabelRepository;

    private TaskList getTaskListOrThrow(Long taskListId) {
        return taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh sách công việc với ID: " + taskListId));
    }

    /**
     * Story #32: Tạo mới Card trong TaskList
     */
    @Override
    @Transactional
    public Card createCard(Long taskListId, String title, User currentUser) {
        TaskList taskList = getTaskListOrThrow(taskListId);
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        String trimmedTitle = title != null ? title.trim() : "";
        if (trimmedTitle.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề thẻ không được để trống!");
        }
        if (trimmedTitle.length() > 255) {
            throw new IllegalArgumentException("Tiêu đề thẻ không được vượt quá 255 ký tự!");
        }

        // Vị trí cuối danh sách, dùng position thưa (bước nhảy 10): max(position hiện có) + 10
        Integer nextPosition = cardRepository.findTopByTaskListIdOrderByPositionDesc(taskListId)
                .map(lastCard -> (lastCard.getPosition() != null ? lastCard.getPosition() : 0) + 10)
                .orElse(10);

        Card card = Card.builder()
                .taskListId(taskListId)
                .title(trimmedTitle)
                .position(nextPosition)
                .createdBy(currentUser.getId())
                .build();

        return cardRepository.save(card);
    }

    /**
     * Story #33: Đổi vị trí Card trong cùng TaskList
     */
    @Override
    @Transactional
    public void reorderCardInList(Long cardId, Integer newPosition, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        if (newPosition == null || newPosition < 0) {
            throw new IllegalArgumentException("Vị trí của thẻ không hợp lệ!");
        }

        card.setPosition(newPosition);
        cardRepository.save(card);
    }

    /**
     * Story #34: Di chuyển Card sang TaskList khác (cùng board)
     */
    @Override
    @Transactional
    public void moveCardToAnotherList(Long cardId, Long targetTaskListId, Integer newPosition, User currentUser) {
        Card card = getCardById(cardId);
        TaskList currentTaskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(currentTaskList.getBoardId(), currentUser);

        if (newPosition == null || newPosition < 0) {
            throw new IllegalArgumentException("Vị trí của thẻ không hợp lệ!");
        }

        TaskList targetTaskList = getTaskListOrThrow(targetTaskListId);
        if (!targetTaskList.getBoardId().equals(currentTaskList.getBoardId())) {
            throw new IllegalArgumentException("Không thể di chuyển thẻ sang danh sách công việc thuộc bảng khác!");
        }

        // Convention 6.14: luôn set FK qua field Long, TUYỆT ĐỐI không gọi card.setTaskList(...)
        card.setTaskListId(targetTaskListId);
        card.setPosition(newPosition);
        cardRepository.save(card);
    }

    /**
     * Story #35: Sửa description của Card
     */
    @Override
    @Transactional
    public Card updateCardDetail(Long cardId, CardUpdateDto dto, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        card.setDescription(dto != null ? dto.getDescription() : null);
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void addCardMember(Long cardId, Long userId, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        if (!boardMemberRepository.existsByBoardIdAndUserId(taskList.getBoardId(), userId)) {
            throw new IllegalArgumentException("Người dùng phải là thành viên của bảng mới có thể được gán vào thẻ!");
        }

        if (cardMemberRepository.existsByCardIdAndUserId(cardId, userId)) {
            throw new IllegalArgumentException("Người dùng đã được gán vào thẻ này!");
        }

        CardMember cardMember = CardMember.builder()
                .cardId(cardId)
                .userId(userId)
                .build();
        cardMemberRepository.save(cardMember);
    }

    @Override
    @Transactional
    public void removeCardMember(Long cardId, Long userId, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        CardMember cardMember = cardMemberRepository.findByCardIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thành viên không thuộc thẻ này!"));
        cardMemberRepository.delete(cardMember);
    }

    @Override
    @Transactional
    public void addCardComment(Long cardId, String content, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        String trimmedContent = content != null ? content.trim() : "";
        if (trimmedContent.isBlank()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống!");
        }

        CardComment comment = CardComment.builder()
                .cardId(cardId)
                .userId(currentUser.getId())
                .content(trimmedContent)
                .build();
        cardCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public void addCardLabel(Long cardId, Long labelId, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        if (!labelRepository.existsById(labelId)) {
            throw new IllegalArgumentException("Không tìm thấy nhãn!");
        }

        if (cardLabelRepository.existsByCardIdAndLabelId(cardId, labelId)) {
            throw new IllegalArgumentException("Nhãn đã được gán vào thẻ này!");
        }

        CardLabel cardLabel = CardLabel.builder()
                .cardId(cardId)
                .labelId(labelId)
                .build();
        cardLabelRepository.save(cardLabel);
    }

    @Override
    @Transactional
    public void removeCardLabel(Long cardId, Long labelId, User currentUser) {
        Card card = getCardById(cardId);
        TaskList taskList = getTaskListOrThrow(card.getTaskListId());
        boardPermissionService.checkEditPermission(taskList.getBoardId(), currentUser);

        if (!cardLabelRepository.existsByCardIdAndLabelId(cardId, labelId)) {
            throw new ResourceNotFoundException("Nhãn không thuộc thẻ này!");
        }
        cardLabelRepository.deleteByCardIdAndLabelId(cardId, labelId);
    }

    @Override
    @Transactional(readOnly = true)
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ với ID: " + cardId));
    }
}
