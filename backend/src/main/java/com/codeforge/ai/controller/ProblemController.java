package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.dto.CreateProblemRequest;
import com.codeforge.ai.dto.ProblemDto;
import com.codeforge.ai.service.ProblemService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problems")
@AllArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProblemDto>>> getAllProblems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProblemDto> problems = problemService.getAllProblems(difficulty, tag, search, pageable);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Problems retrieved successfully", problems));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<ProblemDto>> getRandomProblem() {
        ProblemDto problem = problemService.getRandomProblem();
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Random problem retrieved successfully", problem));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ApiResponse<ProblemDto>> getProblemByIdentifier(@PathVariable String identifier) {
        ProblemDto problem = problemService.getProblemByIdentifier(identifier);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Problem retrieved successfully", problem));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProblemDto>>> searchProblems(@RequestParam String query) {
        List<ProblemDto> problems = problemService.searchProblems(query);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Search completed successfully", problems));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProblemDto>> createProblem(@Valid @RequestBody CreateProblemRequest request) {
        ProblemDto problem = problemService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Problem created successfully", problem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProblemDto>> updateProblem(
            @PathVariable Long id,
            @Valid @RequestBody CreateProblemRequest request) {
        ProblemDto problem = problemService.updateProblem(id, request);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Problem updated successfully", problem));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Problem deleted successfully", ""));
    }
}