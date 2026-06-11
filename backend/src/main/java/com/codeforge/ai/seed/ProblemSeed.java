package com.codeforge.ai.seed;

import com.codeforge.ai.dto.ProblemExampleDto;
import com.codeforge.ai.dto.ProblemTestCaseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemSeed {
    private String title;
    private String slug;
    private String difficulty;
    private String category;
    private List<String> tags;
    private String description;
    private List<String> constraints;
    private List<String> hints;
    private List<ProblemExampleDto> examples;
    private String starterCodeJava;
    private String starterCodeCpp;
    private String starterCodePython;
    private List<ProblemTestCaseDto> visibleTestCases;
    private List<ProblemTestCaseDto> hiddenTestCases;
    private String solutionTemplate;
    private Integer orderIndex;
}
