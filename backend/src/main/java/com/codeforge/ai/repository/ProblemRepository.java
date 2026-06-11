package com.codeforge.ai.repository;

import com.codeforge.ai.entity.Category;
import com.codeforge.ai.entity.Difficulty;
import com.codeforge.ai.entity.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Page<Problem> findByCategory(Category category, Pageable pageable);
    Page<Problem> findByDifficulty(Difficulty difficulty, Pageable pageable);
    List<Problem> findByTitleContainingIgnoreCase(String title);
    Optional<Problem> findBySlug(String slug);

    @Query(value = "SELECT DISTINCT p FROM Problem p LEFT JOIN p.tags t WHERE " +
            "(:difficulty IS NULL OR p.difficulty = :difficulty) AND " +
            "(:tag IS NULL OR LOWER(t.tag) = LOWER(:tag)) AND " +
            "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.tag) LIKE LOWER(CONCAT('%', :search, '%'))) ",
            countQuery = "SELECT count(DISTINCT p) FROM Problem p LEFT JOIN p.tags t WHERE " +
                    "(:difficulty IS NULL OR p.difficulty = :difficulty) AND " +
                    "(:tag IS NULL OR LOWER(t.tag) = LOWER(:tag)) AND " +
                    "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(t.tag) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<Problem> search(@Param("difficulty") Difficulty difficulty,
                         @Param("tag") String tag,
                         @Param("search") String search,
                         Pageable pageable);

    Page<Problem> findByDifficultyAndCategory(Difficulty difficulty, Category category, Pageable pageable);
}
