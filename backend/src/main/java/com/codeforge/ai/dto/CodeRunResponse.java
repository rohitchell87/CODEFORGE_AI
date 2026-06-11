package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
