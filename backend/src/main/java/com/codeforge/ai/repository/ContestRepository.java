package com.codeforge.ai.repository;

import com.codeforge.ai.entity.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    Page<Contest> findAll(Pageable pageable);
    boolean existsByTitle(String title);
}
