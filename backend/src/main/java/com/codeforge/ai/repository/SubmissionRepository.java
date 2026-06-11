package com.codeforge.ai.repository;

import com.codeforge.ai.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Page<Submission> findByUserId(Long userId, Pageable pageable);
    List<Submission> findByProblemIdAndUserId(Long problemId, Long userId);
    long countByUserIdAndIsAcceptedTrue(Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndIsAcceptedTrueAndSubmittedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<Submission> findByUserIdAndIsAcceptedTrueOrderBySubmittedAtDesc(Long userId, Pageable pageable);
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId, Pageable pageable);
    boolean existsByProblemIdAndUserIdAndIsAcceptedTrue(Long problemId, Long userId);
}
