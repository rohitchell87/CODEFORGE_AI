package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiHintRequest {
    private Long problemId;
    private String problemTitle;
    private String problemDescription;
    private String userCode;
    private String hintType;
    private String difficulty;
}
