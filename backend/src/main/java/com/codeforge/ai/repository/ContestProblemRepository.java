package com.codeforge.ai.repository;

import com.codeforge.ai.entity.ContestProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {
}
