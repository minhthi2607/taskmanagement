package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.CardAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardAttachmentRepository extends JpaRepository<CardAttachment, Long> {

    List<CardAttachment> findByCardIdOrderByUploadedAtDesc(Long cardId);
}
