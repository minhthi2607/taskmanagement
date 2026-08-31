package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.CardWatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardWatcherRepository extends JpaRepository<CardWatcher, Long> {
    
    boolean existsByCardIdAndUserId(Long cardId, Long userId);
    
    Optional<CardWatcher> findByCardIdAndUserId(Long cardId, Long userId);
    
    List<CardWatcher> findByCardId(Long cardId);
    
    void deleteByCardIdAndUserId(Long cardId, Long userId);
}
