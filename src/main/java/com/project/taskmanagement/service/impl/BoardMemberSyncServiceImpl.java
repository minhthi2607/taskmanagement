package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.Board;
import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.entity.TeamMember;
import com.project.taskmanagement.enums.BoardVisibility;
import com.project.taskmanagement.enums.Role;
import com.project.taskmanagement.repository.BoardMemberRepository;
import com.project.taskmanagement.repository.BoardRepository;
import com.project.taskmanagement.repository.TeamMemberRepository;
import com.project.taskmanagement.service.BoardMemberSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardMemberSyncServiceImpl implements BoardMemberSyncService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    @Transactional
    public void syncBoardMembersForGroupBoard(Board board) {
        if (board == null || board.getId() == null || board.getTeamId() == null) {
            return;
        }

        if (board.getVisibility() != BoardVisibility.GROUP) {
            return;
        }

        List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(board.getTeamId());
        for (TeamMember tm : teamMembers) {
            if (!boardMemberRepository.existsByBoardIdAndUserId(board.getId(), tm.getUserId())) {
                BoardMember boardMember = BoardMember.builder()
                        .boardId(board.getId())
                        .userId(tm.getUserId())
                        .role(Role.MEMBER)
                        .joinedAt(LocalDateTime.now())
                        .build();
                boardMemberRepository.save(boardMember);
            }
        }
    }

    @Override
    @Transactional
    public void addMemberToAllGroupBoardsOfTeam(Long teamId, Long userId) {
        if (teamId == null || userId == null) {
            return;
        }

        List<Board> groupBoards = boardRepository.findByTeamIdOrderByCreatedAtDesc(teamId).stream()
                .filter(b -> b.getVisibility() == BoardVisibility.GROUP)
                .toList();

        for (Board board : groupBoards) {
            if (!boardMemberRepository.existsByBoardIdAndUserId(board.getId(), userId)) {
                BoardMember boardMember = BoardMember.builder()
                        .boardId(board.getId())
                        .userId(userId)
                        .role(Role.MEMBER)
                        .joinedAt(LocalDateTime.now())
                        .build();
                boardMemberRepository.save(boardMember);
            }
        }
    }

    @Override
    @Transactional
    public void removeMemberFromAllGroupBoardsOfTeam(Long teamId, Long userId) {
        if (teamId == null || userId == null) {
            return;
        }

        List<Board> groupBoards = boardRepository.findByTeamIdOrderByCreatedAtDesc(teamId).stream()
                .filter(b -> b.getVisibility() == BoardVisibility.GROUP)
                .toList();

        for (Board board : groupBoards) {
            if (userId.equals(board.getCreatedBy())) {
                continue;
            }
            boardMemberRepository.findByBoardIdAndUserId(board.getId(), userId)
                    .ifPresent(boardMemberRepository::delete);
        }
    }
}
