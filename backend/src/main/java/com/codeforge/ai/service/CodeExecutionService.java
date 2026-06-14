package com.codeforge.ai.service;

import com.codeforge.ai.dto.CodeRunRequest;
import com.codeforge.ai.dto.CodeRunResponse;
import com.codeforge.ai.dto.CodeSubmissionCaseResult;
import com.codeforge.ai.dto.CodeSubmissionResult;
import com.codeforge.ai.dto.CreateSubmissionRequest;
import com.codeforge.ai.dto.ProblemDto;
import com.codeforge.ai.dto.ProblemExampleDto;
import com.codeforge.ai.dto.ProblemTestCaseDto;
import com.codeforge.ai.exception.SubmissionConfigurationException;
import com.codeforge.ai.service.Judge0Service;
import com.codeforge.ai.service.ProblemService;
import com.codeforge.ai.service.SubmissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CodeExecutionService {

    private final Judge0Service judge0Service;
    private final ProblemService problemService;
    private final SubmissionService submissionService;

    public CodeRunResponse runCode(CodeRunRequest request) {
        if (request.getProblemId() == null) {
            return judge0Service.run(request);
        }

        ProblemDto problem = problemService.getProblemById(request.getProblemId());
        List<ProblemTestCaseDto> sampleTests = problem.getVisibleTestCases();
        if (sampleTests == null || sampleTests.isEmpty()) {
            sampleTests = buildSampleTests(problem);
        }

        if (sampleTests == null || sampleTests.isEmpty()) {
            return judge0Service.run(request);
        }

        List<CodeSubmissionCaseResult> caseResults = new ArrayList<>();
        int passed = 0;
        double maxRuntime = 0;
        int maxMemory = 0;

        String lastStdout = null;
        String lastStderr = null;
        String lastCompileOutput = null;

        for (int i = 0; i < sampleTests.size(); i++) {
            ProblemTestCaseDto sampleTest = sampleTests.get(i);
            log.info("Executing sample test {}/{} for problemId={}", i + 1, sampleTests.size(), request.getProblemId());
            CodeRunRequest runRequest = new CodeRunRequest(null, request.getLanguage(), request.getCode(), sampleTest.getInput());
            CodeRunResponse runResponse = judge0Service.run(runRequest);

            lastStdout = runResponse.getStdout();
            lastStderr = runResponse.getStderr();
            lastCompileOutput = runResponse.getCompileOutput();

            String actualOutput = normalize(runResponse.getStdout());
            String expectedOutput = normalize(sampleTest.getExpectedOutput());

            String caseStatus;
            if (runResponse.getCompileOutput() != null && !runResponse.getCompileOutput().isEmpty()) {
                caseStatus = "Compilation Error";
            } else if (runResponse.getStderr() != null && !runResponse.getStderr().isEmpty()) {
                caseStatus = "Runtime Error";
            } else if (!actualOutput.equals(expectedOutput)) {
                caseStatus = "Wrong Answer";
            } else {
                caseStatus = "Passed";
            }

            if ("Passed".equals(caseStatus)) {
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

            log.info("Sample test {}/{} response: status={} runtime={} memory={} compileOutput={} stderr={} stdout={}",
                    i + 1,
                    sampleTests.size(),
                    runResponse.getStatus(),
                    runResponse.getRuntime(),
                    runResponse.getMemory(),
                    runResponse.getCompileOutput(),
                    runResponse.getStderr(),
                    runResponse.getStdout());

            caseResults.add(new CodeSubmissionCaseResult(
                    "Sample " + (i + 1),
                    caseStatus,
                    sampleTest.getInput(),
                    actualOutput,
                    expectedOutput
            ));
        }

        String finalStatus = passed == sampleTests.size() ? "Accepted" : "Wrong Answer";
        CodeRunResponse response = new CodeRunResponse();
        String outputSummary = caseResults.stream()
                .map(resultCase -> resultCase.getName() + "\nOutput: " + resultCase.getOutput())
                .collect(Collectors.joining("\n---\n"));
        response.setOutput(outputSummary);
        response.setStdout(lastStdout);
        response.setStderr(lastStderr);
        response.setCompileOutput(lastCompileOutput);
        response.setRuntime(String.format("%.2f s", maxRuntime));
        response.setMemory(maxMemory + " KB");
        response.setStatus(finalStatus);
        response.setPassed(passed);
        response.setTotal(sampleTests.size());
        response.setCases(caseResults);
        return response;
    }

    private List<ProblemTestCaseDto> buildSampleTests(ProblemDto problem) {
        if (problem.getExampleInput() != null && problem.getExampleOutput() != null) {
            return List.of(ProblemTestCaseDto.builder()
                    .input(problem.getExampleInput())
                    .expectedOutput(problem.getExampleOutput())
                    .build());
        }
        if (problem.getExamples() != null && !problem.getExamples().isEmpty()) {
            ProblemExampleDto example = problem.getExamples().get(0);
            return List.of(ProblemTestCaseDto.builder()
                    .input(example.getInput())
                    .expectedOutput(example.getOutput())
                    .build());
        }
        return List.of();
    }

    public CodeSubmissionResult submitCode(Long userId, CreateSubmissionRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required for submission.");
        }
        if (request == null || request.getProblemId() == null) {
            throw new IllegalArgumentException("Problem ID is required for submission.");
        }

        ProblemDto problem = problemService.getProblemById(request.getProblemId());
        if (problem == null) {
            throw new IllegalArgumentException("Problem not found for id: " + request.getProblemId());
        }

        List<ProblemTestCaseDto> sampleTests = problem.getVisibleTestCases();
        if (sampleTests == null || sampleTests.isEmpty()) {
            log.error("Submission failed validation: missing sample test cases for problemId={}", request.getProblemId());
            throw new SubmissionConfigurationException("No sample test cases configured for this problem");
        }

        List<ProblemTestCaseDto> hidden = problem.getHiddenTestCases();
        if (hidden == null || hidden.isEmpty()) {
            log.error("Submission failed validation: missing hidden test cases for problemId={}", request.getProblemId());
            throw new SubmissionConfigurationException("No hidden test cases configured for this problem");
        }

        log.info("Beginning submission execution: userId={} problemId={} hiddenTestCount={} sampleTestCount={}",
                userId, request.getProblemId(), hidden.size(), sampleTests.size());

        List<CodeSubmissionCaseResult> caseResults = new ArrayList<>();
        int passed = 0;
        double maxRuntime = 0;
        int maxMemory = 0;

        for (int index = 0; index < hidden.size(); index++) {
            ProblemTestCaseDto hiddenTest = hidden.get(index);
            log.info("Executing hidden test {}/{} for problemId={} userId={}", index + 1, hidden.size(), request.getProblemId(), userId);
            CodeRunRequest runRequest = new CodeRunRequest(null, request.getLanguage(), request.getCode(), hiddenTest.getInput());
            CodeRunResponse runResponse = judge0Service.run(runRequest);

            log.info("Hidden test {} response: status={} runtime={} memory={} compileOutput={} stderr={} stdout={}",
                    index + 1,
                    runResponse.getStatus(),
                    runResponse.getRuntime(),
                    runResponse.getMemory(),
                    runResponse.getCompileOutput(),
                    runResponse.getStdout());

            String actualOutput = normalize(runResponse.getCompileOutput() != null && !runResponse.getCompileOutput().isEmpty()
                    ? runResponse.getCompileOutput()
                    : runResponse.getStderr() != null && !runResponse.getStderr().isEmpty()
                    ? runResponse.getStderr()
                    : runResponse.getStdout() != null && !runResponse.getStdout().isEmpty()
                    ? runResponse.getStdout()
                    : runResponse.getOutput());
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
                caseStatus = "Accepted";
            }

            if (caseStatus.equals("Accepted")) {
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

            String actualCaseOutput = normalize(runResponse.getStdout());

            caseResults.add(new CodeSubmissionCaseResult(
                    "Test " + (index + 1),
                    caseStatus,
                    hiddenTest.getInput(),
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

        log.info("Submission execution complete: userId={} problemId={} status={} passed={}/{} runtime={} memory={}",
                userId, request.getProblemId(), submissionResult.getStatus(), submissionResult.getPassed(), submissionResult.getTotal(), submissionResult.getRuntime(), submissionResult.getMemory());

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
