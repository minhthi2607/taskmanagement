package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.CardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardCommentRepository extends JpaRepository<CardComment, Long> {

    List<CardComment> findByCardIdOrderByCreatedAtAsc(Long cardId);
}
