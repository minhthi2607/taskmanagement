package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    List<TaskList> findByBoardIdOrderByPositionAsc(Long boardId);

    Optional<TaskList> findTopByBoardIdOrderByPositionDesc(Long boardId);

    boolean existsByBoardIdAndName(Long boardId, String name);
}
