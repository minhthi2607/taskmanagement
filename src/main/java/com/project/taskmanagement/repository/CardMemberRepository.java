package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.CardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardMemberRepository extends JpaRepository<CardMember, Long> {

    List<CardMember> findByCardId(Long cardId);

    Optional<CardMember> findByCardIdAndUserId(Long cardId, Long userId);

    boolean existsByCardIdAndUserId(Long cardId, Long userId);
}
