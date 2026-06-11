package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDto {
    private Long id;
    private Long userId;
    private Long problemId;
    private String problemTitle;
    private String code;
    private String output;
    private Boolean isAccepted;
    private Double executionTime;
    private Double memoryUsage;
    private String language;
    private String executionStatus;
    private Integer passedTests;
    private Integer totalTests;
    private String submittedAt;
}
