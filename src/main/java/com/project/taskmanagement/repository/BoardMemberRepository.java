package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    boolean existsByBoardIdAndUserId(Long boardId, Long userId);
}
