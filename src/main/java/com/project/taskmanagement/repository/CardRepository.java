package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByTaskListIdOrderByPositionAsc(Long taskListId);

    Optional<Card> findTopByTaskListIdOrderByPositionDesc(Long taskListId);
}
