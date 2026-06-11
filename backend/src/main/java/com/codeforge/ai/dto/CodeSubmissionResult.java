package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmissionResult {
    private String status;
    private String runtime;
    private String memory;
    private int passed;
    private int total;
    private List<CodeSubmissionCaseResult> cases;
}
