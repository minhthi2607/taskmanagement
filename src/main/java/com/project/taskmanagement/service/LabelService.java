package com.project.taskmanagement.service;

import com.project.taskmanagement.entity.Label;
import com.project.taskmanagement.entity.User;

import java.util.List;

public interface LabelService {

    List<Label> getLabelsByBoardId(Long boardId);

    Label createLabel(Long boardId, String name, String color, User currentUser);

    Label updateLabel(Long labelId, String name, String color, User currentUser);

    void deleteLabel(Long labelId, User currentUser);
}
