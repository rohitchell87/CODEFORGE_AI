package com.codeforge.ai.repository;

import com.codeforge.ai.entity.ContestParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ContestParticipationRepository extends JpaRepository<ContestParticipation, Long> {
    Page<ContestParticipation> findByContestIdOrderByScoreDescRankAsc(Long contestId, Pageable pageable);
    Optional<ContestParticipation> findByContestIdAndUserId(Long contestId, Long userId);
    List<ContestParticipation> findByUserIdOrderByJoinedAtDesc(Long userId);
}
