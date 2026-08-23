package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardLabelRepository extends JpaRepository<CardLabel, Long> {

    List<CardLabel> findByCardId(Long cardId);

    boolean existsByCardIdAndLabelId(Long cardId, Long labelId);

    Optional<CardLabel> findByCardIdAndLabelId(Long cardId, Long labelId);

    void deleteByCardIdAndLabelId(Long cardId, Long labelId);
}
