package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Card;
import com.project.taskmanagement.entity.CardWatcher;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.CardRepository;
import com.project.taskmanagement.repository.CardWatcherRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.CardWatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardWatcherServiceImpl implements CardWatcherService {

    private final CardWatcherRepository cardWatcherRepository;
    private final CardRepository cardRepository;
    private final BoardPermissionService boardPermissionService;

    @Override
    @Transactional
    public boolean toggleWatchCard(Long cardId, User currentUser) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thẻ với ID: " + cardId));
                
        // Kiểm tra quyền truy cập board thông qua TaskList -> Board
        Long boardId = card.getTaskList().getBoardId();
        boardPermissionService.checkEditPermission(boardId, currentUser);

        return cardWatcherRepository.findByCardIdAndUserId(cardId, currentUser.getId())
                .map(watcher -> {
                    cardWatcherRepository.delete(watcher);
                    return false;
                })
                .orElseGet(() -> {
                    CardWatcher newWatcher = CardWatcher.builder()
                            .cardId(cardId)
                            .userId(currentUser.getId())
                            .build();
                    cardWatcherRepository.save(newWatcher);
                    return true;
                });
    }

    @Override
    public boolean isWatching(Long cardId, Long userId) {
        return cardWatcherRepository.existsByCardIdAndUserId(cardId, userId);
    }

    @Override
    public List<CardWatcher> getWatchers(Long cardId) {
        return cardWatcherRepository.findByCardId(cardId);
    }
}
