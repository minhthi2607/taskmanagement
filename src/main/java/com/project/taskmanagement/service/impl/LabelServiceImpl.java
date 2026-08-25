package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.Label;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.LabelRepository;
import com.project.taskmanagement.service.BoardPermissionService;
import com.project.taskmanagement.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.project.taskmanagement.exception.LabelNotFoundException;
import com.project.taskmanagement.repository.CardLabelRepository;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final BoardRepository boardRepository;
    private final BoardPermissionService boardPermissionService;
    private final CardLabelRepository cardLabelRepository;

    @Override
    public List<Label> getLabelsByBoardId(Long boardId) {
        return labelRepository.findByBoardIdOrderByNameAsc(boardId);
    }

    @Override
    @Transactional
    public Label createLabel(Long boardId, String name, String color, User currentUser) {
        boardPermissionService.checkCardMemberOrLabelPermission(boardId, currentUser);

        String trimmedName = (name != null) ? name.trim() : "";
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Tên nhãn không được để trống!");
        }
        if (trimmedName.length() > 50) {
            throw new IllegalArgumentException("Tên nhãn không được vượt quá 50 ký tự!");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bảng công việc"));

        Label label = Label.builder()
                .boardId(board.getId())
                .name(name)
                .color(color)
                .build();

        return labelRepository.save(label);
    }

    @Override
    @Transactional
    public Label updateLabel(Long labelId, String name, String color, User currentUser) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Không tìm thấy nhãn"));

        boardPermissionService.checkCardMemberOrLabelPermission(label.getBoardId(), currentUser);

        String trimmedName = (name != null) ? name.trim() : "";
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Tên nhãn không được để trống!");
        }
        if (trimmedName.length() > 50) {
            throw new IllegalArgumentException("Tên nhãn không được vượt quá 50 ký tự!");
        }

        label.setName(name);
        label.setColor(color);
        return labelRepository.save(label);
    }

    @Override
    @Transactional
    public void deleteLabel(Long labelId, User currentUser) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Không tìm thấy nhãn"));

        boardPermissionService.checkCardMemberOrLabelPermission(label.getBoardId(), currentUser);

        cardLabelRepository.deleteByLabelId(labelId);
        labelRepository.delete(label);
    }
}
