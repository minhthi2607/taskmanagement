package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>,JpaSpecificationExecutor<Card> {

    List<Card> findByTaskListIdOrderByPositionAsc(Long taskListId);

    Optional<Card> findTopByTaskListIdOrderByPositionDesc(Long taskListId);
    @Query("SELECT DISTINCT c FROM Card c LEFT JOIN FETCH c.members WHERE c.dueDate IS NOT NULL AND c.reminderSentAt IS NULL")
    List<Card> findPendingReminderCards();

}
