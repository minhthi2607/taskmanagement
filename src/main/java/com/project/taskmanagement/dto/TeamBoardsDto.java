package com.project.taskmanagement.dto;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.Team;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamBoardsDto {

    private Team team;

    @Builder.Default
    private List<Board> boards = new ArrayList<>();
}
