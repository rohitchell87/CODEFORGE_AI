package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.dto.CreateSubmissionRequest;
import com.codeforge.ai.dto.SubmissionDto;
import com.codeforge.ai.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.codeforge.ai.security.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/submissions")
@AllArgsConstructor
@Slf4j
public class SubmissionController {

    private SubmissionService submissionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubmissionDto>> createSubmission(@Valid @RequestBody CreateSubmissionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        SubmissionDto submission = submissionService.createSubmission(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Submission created successfully", submission));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubmissionDto>> getSubmissionById(@PathVariable Long id) {
        SubmissionDto submission = submissionService.getSubmissionById(id);
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Submission retrieved successfully", submission));
    }

    @GetMapping("/user/my-submissions")
    public ResponseEntity<ApiResponse<Page<SubmissionDto>>> getUserSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        System.out.println("SUBMISSIONS ENDPOINT HIT");
        System.out.println("QUERY START: page=" + page + ", size=" + size);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + auth);
            Object principal = auth != null ? auth.getPrincipal() : null;
            System.out.println("Auth Principal: " + principal);
            System.out.println("Auth Principal Class: " + (principal != null ? principal.getClass().getName() : "NULL"));

            Pageable pageable = PageRequest.of(page, size);
            System.out.println("Pageable created: " + pageable);

            if (!(principal instanceof UserPrincipal)) {
                System.out.println("USER ID = null (anonymous request)");
                System.out.println("QUERY END: returning empty submissions page for anonymous user");
                return ResponseEntity.ok()
                        .body(new ApiResponse<>(HttpStatus.OK.value(), "No authenticated user, returning empty submissions page.", Page.empty(pageable)));
            }

            UserPrincipal userPrincipal = (UserPrincipal) principal;
            Long userId = userPrincipal.getId();
            String email = userPrincipal.getEmail();
            System.out.println("USER ID = " + userId);
            System.out.println("User Email: " + email);

            Page<SubmissionDto> submissions = submissionService.getUserSubmissions(userId, pageable);
            System.out.println("Total submissions returned: " + submissions.getTotalElements());
            System.out.println("Total pages: " + submissions.getTotalPages());
            System.out.println("Current page content size: " + submissions.getContent().size());
            System.out.println("QUERY END: submissions query completed successfully");

            return ResponseEntity.ok()
                    .body(new ApiResponse<>(HttpStatus.OK.value(), "Submissions retrieved successfully", submissions));
        } catch (Exception e) {
            System.out.println("EXCEPTION IN getUserSubmissions:");
            System.out.println("Exception Class: " + e.getClass().getName());
            System.out.println("Exception Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/problem/{problemId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SubmissionDto>>> getProblemSubmissions(@PathVariable Long problemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        
        List<SubmissionDto> submissions = submissionService.getProblemSubmissions(problemId, userPrincipal.getId());
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Submissions retrieved successfully", submissions));
    }
}