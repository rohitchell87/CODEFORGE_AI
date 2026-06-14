package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmissionCaseResult {
    private String name;
    private String status;
    private String input;
    private String output;
    private String expected;
}
