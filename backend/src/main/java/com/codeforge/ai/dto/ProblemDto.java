package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemDto {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String difficulty;
    private String category;
    private List<ProblemExampleDto> examples;
    private String exampleInput;
    private String exampleOutput;
    private String sampleSolution;
    private List<String> constraints;
    private List<String> hints;
    private String starterCodeJava;
    private String starterCodeCpp;
    private String starterCodePython;
    private List<ProblemTestCaseDto> visibleTestCases;
    private List<ProblemTestCaseDto> hiddenTestCases;
    private String solutionTemplate;
    private Integer orderIndex;
    private Integer acceptanceRate;
    private Integer submissionCount;
    private Integer acceptedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
}
