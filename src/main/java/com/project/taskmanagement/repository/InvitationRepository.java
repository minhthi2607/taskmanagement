package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Invitation;
import com.project.taskmanagement.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);

    List<Invitation> findByTeamIdAndStatus(Long teamId, InvitationStatus status);

    Optional<Invitation> findByTeamIdAndEmailAndStatus(Long teamId, String email, InvitationStatus status);

    List<Invitation> findByBoardIdAndStatus(Long boardId, InvitationStatus status);

    Optional<Invitation> findByBoardIdAndEmailAndStatus(Long boardId, String email, InvitationStatus status);
}
