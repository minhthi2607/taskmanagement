package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Team;
import com.project.taskmanagement.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN t.members m WHERE t.createdBy = :userId OR m.userId = :userId ORDER BY t.name ASC")
    List<Team> findUserTeamsOrderByNameAsc(@Param("userId") Long userId);

    List<Team> findAllByOrderByNameAsc();

    List<Team> findByVisibilityOrderByNameAsc(Visibility visibility);
}
