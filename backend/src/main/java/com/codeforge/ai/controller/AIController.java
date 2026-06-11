package com.codeforge.ai.controller;

import com.codeforge.ai.dto.ApiResponse;
import com.codeforge.ai.dto.AiHintRequest;
import com.codeforge.ai.dto.AiResponse;
import com.codeforge.ai.service.AIService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class AIController {

    private AIService aiService;

    @PostMapping("/hint")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AiResponse>> generateHint(@Valid @RequestBody AiHintRequest request) {
        AiResponse response = aiService.generateHint(request.getProblemId());
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Hint generated successfully", response));
    }

    @PostMapping("/explain")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AiResponse>> explainSolution(@Valid @RequestBody AiHintRequest request) {
        AiResponse response = aiService.explainSolution(request.getProblemId());
        return ResponseEntity.ok()
                .body(new ApiResponse<>(HttpStatus.OK.value(), "Solution explanation generated", response));
    }
}