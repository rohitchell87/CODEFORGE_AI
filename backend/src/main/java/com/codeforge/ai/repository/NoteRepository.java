package com.codeforge.ai.repository;

import com.codeforge.ai.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUserId(Long userId, Pageable pageable);
    Optional<Note> findByIdAndUserId(Long id, Long userId);
    List<Note> findByProblemIdAndUserId(Long problemId, Long userId);
}
