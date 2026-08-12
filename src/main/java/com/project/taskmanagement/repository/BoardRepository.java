package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.taskmanagement.enums.BoardVisibility;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByTeamIdOrderByCreatedAtDesc(Long teamId);

    List<Board> findByCreatedByOrderByCreatedAtDesc(Long userId);
    List<Board> findByTeamIdAndVisibilityOrderByCreatedAtDesc(Long teamId, BoardVisibility visibility);
}
