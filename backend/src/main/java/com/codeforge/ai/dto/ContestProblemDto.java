package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestProblemDto {
    private Long id;
    private Long problemId;
    private String title;
    private String difficulty;
    private String category;
    private Integer orderIndex;
    private Integer points;
}
