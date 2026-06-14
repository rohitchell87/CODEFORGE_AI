package com.codeforge.ai.controller;

import com.codeforge.ai.dto.CodeRunRequest;
import com.codeforge.ai.dto.CodeRunResponse;
import com.codeforge.ai.dto.CodeSubmissionResult;
import com.codeforge.ai.dto.CreateSubmissionRequest;
import com.codeforge.ai.exception.SubmissionConfigurationException;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.security.UserPrincipal;
import com.codeforge.ai.service.CodeExecutionService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/code")
@AllArgsConstructor
@Slf4j
public class CodeExecutionController {

    private final CodeExecutionService executionService;

    @PostMapping("/run")
    public ResponseEntity<CodeRunResponse> runCode(@RequestBody CodeRunRequest request) {
        log.info("RUN ENDPOINT HIT for problemId={}", request.getProblemId());
        CodeRunResponse runResult = executionService.runCode(request);
        return ResponseEntity.ok(runResult);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> submitCode(@RequestBody CreateSubmissionRequest request, HttpServletRequest httpRequest) {
        System.out.println("\n=== CODE EXECUTION SUBMIT ENDPOINT HIT ===");
        log.info("REQUEST RECEIVED /code/submit");
        log.info("BACKEND SUBMIT HIT for problemId={} language={} requestUri={}", request.getProblemId(), request.getLanguage(), httpRequest.getRequestURI());
        log.info("PROBLEM ID = {}", request.getProblemId());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("AUTHENTICATION OBJECT = " + auth);
        System.out.println("AUTHENTICATION CLASS = " + (auth != null ? auth.getClass().getName() : "NULL"));
        System.out.println("AUTH PRINCIPAL = " + (auth != null ? auth.getPrincipal() : "NULL"));
        System.out.println("AUTH PRINCIPAL CLASS = " + (auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "NULL"));
        System.out.println("AUTH NAME = " + (auth != null ? auth.getName() : "null"));
        System.out.println("AUTH AUTHORITIES = " + (auth != null ? auth.getAuthorities() : "null"));
        System.out.println("IS AUTHENTICATED = " + (auth != null ? auth.isAuthenticated() : false));
        
        log.info("AUTH USER = {}", auth != null ? auth.getName() : "null");
        log.info("AUTHORITIES = {}", auth != null ? auth.getAuthorities() : "null");
        log.info("REQUEST URI = {}", httpRequest.getRequestURI());

        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            System.out.println("SUBMIT REJECTED: NO AUTHENTICATED USER");
            log.warn("Submit attempted without authenticated user: auth={}", auth);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to submit code"));
        }

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        System.out.println("SUBMIT ACCEPTED FOR USER: " + userPrincipal.getEmail());
        System.out.println("USER ID = " + userPrincipal.getId());
        System.out.println("USER ROLE = " + userPrincipal.getRole());
        
        log.info("Authenticated submit by userId={} email={}", userPrincipal.getId(), userPrincipal.getEmail());

        try {
            CodeSubmissionResult submissionResult = executionService.submitCode(userPrincipal.getId(), request);
            log.info("Submission result: status={} passed={}/{} runtime={} memory={}",
                    submissionResult.getStatus(), submissionResult.getPassed(), submissionResult.getTotal(),
                    submissionResult.getRuntime(), submissionResult.getMemory());

            System.out.println("SUBMISSION COMPLETED SUCCESSFULLY");
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", submissionResult.getStatus());
            responseBody.put("stdout", submissionResult.getCases() != null && !submissionResult.getCases().isEmpty()
                    ? submissionResult.getCases().get(0).getOutput() : null);
            responseBody.put("runtime", submissionResult.getRuntime());
            responseBody.put("memory", submissionResult.getMemory());
            responseBody.put("passed", submissionResult.getPassed());
            responseBody.put("total", submissionResult.getTotal());
            responseBody.put("cases", submissionResult.getCases());
            return ResponseEntity.ok(responseBody);
        } catch (SubmissionConfigurationException ex) {
            log.error("Submission configuration error for userId={} problemId={}: {}", userPrincipal.getId(), request.getProblemId(), ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "CONFIG_ERROR",
                    "message", ex.getMessage()
            ));
        } catch (ResourceNotFoundException ex) {
            log.error("Submission resource not found for userId={} problemId={}: {}", userPrincipal.getId(), request.getProblemId(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "ERROR",
                    "message", ex.getMessage()
            ));
        } catch (IllegalArgumentException ex) {
            log.error("Submission validation error for userId={} problemId={}: {}", userPrincipal.getId(), request.getProblemId(), ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            log.error("Unexpected submission error for userId={} problemId={}: {}", userPrincipal.getId(), request.getProblemId(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "ERROR",
                    "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error during submission"));
        }
    }
}





