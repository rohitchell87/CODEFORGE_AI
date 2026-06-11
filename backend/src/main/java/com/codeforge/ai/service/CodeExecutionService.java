package com.codeforge.ai.service;

import com.codeforge.ai.dto.CodeRunRequest;
import com.codeforge.ai.dto.CodeRunResponse;
import com.codeforge.ai.dto.CodeSubmissionCaseResult;
import com.codeforge.ai.dto.CodeSubmissionResult;
import com.codeforge.ai.dto.CreateSubmissionRequest;
import com.codeforge.ai.dto.ProblemDto;
import com.codeforge.ai.dto.ProblemExampleDto;
import com.codeforge.ai.dto.ProblemTestCaseDto;
import com.codeforge.ai.service.Judge0Service;
import com.codeforge.ai.service.ProblemService;
import com.codeforge.ai.service.SubmissionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CodeExecutionService {

    private final Judge0Service judge0Service;
    private final ProblemService problemService;
    private final SubmissionService submissionService;

    public CodeRunResponse runCode(CodeRunRequest request) {
        return judge0Service.run(request);
    }

    public CodeSubmissionResult submitCode(Long userId, CreateSubmissionRequest request) {
        ProblemDto problem = problemService.getProblemById(request.getProblemId());
        List<ProblemTestCaseDto> hidden = problem.getHiddenTestCases();
        if (hidden == null || hidden.isEmpty()) {
            hidden = buildHiddenTests(problem);
        }

        List<CodeSubmissionCaseResult> caseResults = new ArrayList<>();
        int passed = 0;
        double maxRuntime = 0;
        int maxMemory = 0;

        for (int index = 0; index < hidden.size(); index++) {
            ProblemTestCaseDto hiddenTest = hidden.get(index);
            CodeRunRequest runRequest = new CodeRunRequest(request.getLanguage(), request.getCode(), hiddenTest.getInput());
            CodeRunResponse runResponse = judge0Service.run(runRequest);

            String actualOutput = normalize(runResponse.getOutput());
            String expectedOutput = normalize(hiddenTest.getExpectedOutput());
            String status = runResponse.getStatus();
            String caseStatus;

            if (status != null && status.toLowerCase().contains("compilation")) {
                caseStatus = "Compilation Error";
            } else if (status != null && status.toLowerCase().contains("runtime")) {
                caseStatus = "Runtime Error";
            } else if (!actualOutput.equals(expectedOutput)) {
                caseStatus = "Wrong Answer";
            } else {
                caseStatus = "Passed";
            }

            if (caseStatus.equals("Passed")) {
                passed++;
            }

            if (runResponse.getRuntime() != null) {
                try {
                    double time = Double.parseDouble(runResponse.getRuntime().replaceAll("[^0-9.]", ""));
                    maxRuntime = Math.max(maxRuntime, time);
                } catch (NumberFormatException ignored) {
                }
            }
            if (runResponse.getMemory() != null) {
                try {
                    int memory = Integer.parseInt(runResponse.getMemory().replaceAll("[^0-9]", ""));
                    maxMemory = Math.max(maxMemory, memory);
                } catch (NumberFormatException ignored) {
                }
            }

            String actualCaseOutput = runResponse.getCompileOutput() != null && !runResponse.getCompileOutput().isEmpty()
                    ? runResponse.getCompileOutput()
                    : runResponse.getStderr() != null && !runResponse.getStderr().isEmpty()
                    ? runResponse.getStderr()
                    : runResponse.getStdout();

            caseResults.add(new CodeSubmissionCaseResult(
                    "Test " + (index + 1),
                    caseStatus,
                    actualCaseOutput,
                    hiddenTest.getExpectedOutput()
            ));
        }

        String finalStatus = passed == hidden.size() ? "Accepted" : passed == 0 ? "Wrong Answer" : "Partial";
        CodeSubmissionResult submissionResult = new CodeSubmissionResult(
                finalStatus,
                String.format("%.2f s", maxRuntime),
                maxMemory + " KB",
                passed,
                hidden.size(),
                caseResults
        );

        submissionService.createSubmission(userId, request, submissionResult);
        return submissionResult;
    }

    private List<ProblemTestCaseDto> buildHiddenTests(ProblemDto problem) {
        List<ProblemTestCaseDto> tests = new ArrayList<>();
        if (problem.getExampleInput() != null && problem.getExampleOutput() != null) {
            tests.add(ProblemTestCaseDto.builder()
                    .input(problem.getExampleInput())
                    .expectedOutput(problem.getExampleOutput())
                    .build());
        } else if (problem.getExamples() != null && !problem.getExamples().isEmpty()) {
            ProblemExampleDto example = problem.getExamples().get(0);
            tests.add(ProblemTestCaseDto.builder()
                    .input(example.getInput())
                    .expectedOutput(example.getOutput())
                    .build());
        }
        if (tests.isEmpty()) {
            tests.add(ProblemTestCaseDto.builder()
                    .input("")
                    .expectedOutput("")
                    .build());
        }
        return tests;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace("\r\n", "\n").replaceAll("\n+$", "");
    }
}
