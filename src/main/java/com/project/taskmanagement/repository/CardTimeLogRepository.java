package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.CardTimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CardTimeLogRepository extends JpaRepository<CardTimeLog, Long> {

    List<CardTimeLog> findByCardIdOrderByLoggedAtDescCreatedAtDesc(Long cardId);

    List<CardTimeLog> findByCardId(Long cardId);

    @Query("SELECT COALESCE(SUM(c.hours), 0) FROM CardTimeLog c WHERE c.cardId = :cardId")
    BigDecimal getTotalHoursByCardId(@Param("cardId") Long cardId);

    @Query("SELECT c.cardId, SUM(c.hours) FROM CardTimeLog c WHERE c.cardId IN :cardIds GROUP BY c.cardId")
    List<Object[]> getTotalHoursByCardIds(@Param("cardIds") List<Long> cardIds);
}
