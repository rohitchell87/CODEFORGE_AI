package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProblemRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    @NotBlank(message = "Category is required")
    private String category;

    private String slug;
    private List<String> tags;
    private List<ProblemExampleDto> examples;
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
}
