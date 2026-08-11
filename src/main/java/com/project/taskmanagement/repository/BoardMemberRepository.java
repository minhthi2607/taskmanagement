package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.BoardMember;
import com.project.taskmanagement.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    boolean existsByBoardIdAndUserId(Long boardId, Long userId);

    boolean existsByBoardIdAndUserIdAndRole(Long boardId, Long userId, Role role);

    Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);

    List<BoardMember> findByBoardId(Long boardId);

    List<BoardMember> findByUserId(Long userId);

    void deleteByBoardIdAndUserId(Long boardId, Long userId);
}
