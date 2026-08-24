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
        return labelRepository.findByBoardId(boardId);
    }

    @Override
    @Transactional
    public Label createLabel(Long boardId, String name, String color, User currentUser) {
        if (!boardPermissionService.canEditBoard(boardId, currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền thêm nhãn vào bảng này");
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

        if (!boardPermissionService.canEditBoard(label.getBoardId(), currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa nhãn trong bảng này");
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

        if (!boardPermissionService.canEditBoard(label.getBoardId(), currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa nhãn trong bảng này");
        }

        cardLabelRepository.deleteByLabelId(labelId);
        labelRepository.delete(label);
    }
}
