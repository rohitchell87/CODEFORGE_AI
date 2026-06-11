package com.codeforge.ai.service;

import com.codeforge.ai.dto.CreateNoteRequest;
import com.codeforge.ai.dto.NoteDto;
import com.codeforge.ai.entity.Note;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.NoteRepository;
import com.codeforge.ai.repository.ProblemRepository;
import com.codeforge.ai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
@Transactional
public class NoteService {

    private NoteRepository noteRepository;
    private UserRepository userRepository;
    private ProblemRepository problemRepository;

    public NoteDto createNote(Long userId, CreateNoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + request.getProblemId()));

        Note note = Note.builder()
                .user(user)
                .problem(problem)
                .content(request.getContent())
                .build();

        note = noteRepository.save(note);
        return mapToDto(note);
    }

    public NoteDto getNoteById(Long noteId, Long userId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        return mapToDto(note);
    }

    public Page<NoteDto> getUserNotes(Long userId, Pageable pageable) {
        return noteRepository.findByUserId(userId, pageable)
                .map(this::mapToDto);
    }

    public NoteDto updateNote(Long noteId, Long userId, CreateNoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        note.setContent(request.getContent());
        note = noteRepository.save(note);
        return mapToDto(note);
    }

    public void deleteNote(Long noteId, Long userId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        noteRepository.delete(note);
    }

    private NoteDto mapToDto(Note note) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return NoteDto.builder()
                .id(note.getId())
                .problemId(note.getProblem().getId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt().format(formatter))
                .updatedAt(note.getUpdatedAt().format(formatter))
                .build();
    }
}
