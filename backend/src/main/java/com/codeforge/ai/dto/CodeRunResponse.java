package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.codeforge.ai.dto.CodeSubmissionCaseResult;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeRunResponse {
    private String output;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private String runtime;
    private String memory;
    private String status;
    private Integer passed;
    private Integer total;
    private List<CodeSubmissionCaseResult> cases;

    public CodeRunResponse(String output, String runtime, String memory, String status) {
        this.output = output;
        this.stdout = null;
        this.stderr = null;
        this.compileOutput = null;
        this.runtime = runtime;
        this.memory = memory;
        this.status = status;
    }
}
