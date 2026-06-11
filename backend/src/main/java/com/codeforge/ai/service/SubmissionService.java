package com.codeforge.ai.service;

import com.codeforge.ai.dto.CodeSubmissionCaseResult;
import com.codeforge.ai.dto.CodeSubmissionResult;
import com.codeforge.ai.dto.CreateSubmissionRequest;
import com.codeforge.ai.dto.SubmissionDto;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.entity.Submission;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.ProblemRepository;
import com.codeforge.ai.repository.SubmissionRepository;
import com.codeforge.ai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class SubmissionService {

    private SubmissionRepository submissionRepository;
    private UserRepository userRepository;
    private ProblemRepository problemRepository;

    public SubmissionDto createSubmission(Long userId, CreateSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + request.getProblemId()));

        // Simulate submission (in real scenario, this would compile and test the code)
        boolean isAccepted = simulateCodeExecution(request.getCode());

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(request.getLanguage() != null ? request.getLanguage() : "Java")
                .isAccepted(isAccepted)
                .executionStatus(isAccepted ? "ACCEPTED" : "WRONG_ANSWER")
                .executionTime(generateExecutionTime()) // Simulated
                .memoryUsage(generateMemoryUsage()) // Simulated
                .problemTitle(problem.getTitle())
                .passedTests(isAccepted ? 1 : 0)
                .totalTests(1)
                .submittedAt(LocalDateTime.now())
                .build();

        log.info("====================");
        log.info("SUBMISSION SAVE START");
        log.info("USER ID: {}", userId);
        log.info("PROBLEM ID: {}", problem.getId());
        log.info("VERDICT: {}", submission.getExecutionStatus());
        log.info("====================");

        submission = submissionRepository.save(submission);

        log.info("====================");
        log.info("SUBMISSION SAVE SUCCESS");
        log.info("SUBMISSION SAVED");
        log.info("SUBMISSION ID: {}", submission.getId());
        log.info("====================");

        // Update problem stats
        problem.updateStats(isAccepted);
        problemRepository.save(problem);

        // Update user stats
        if (isAccepted) {
            boolean isFirstAcceptance = !submissionRepository.existsByProblemIdAndUserIdAndIsAcceptedTrue(
                    problem.getId(), user.getId());
            if (isFirstAcceptance) {
                user.setProblemsSolved(user.getProblemsSolved() + 1);
                user.setLastSolvedDate(LocalDateTime.now());
                userRepository.save(user);
            }
        }

        return mapToDto(submission);
    }

    public SubmissionDto createSubmission(Long userId, CreateSubmissionRequest request, CodeSubmissionResult result) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + request.getProblemId()));

        boolean accepted = "Accepted".equalsIgnoreCase(result.getStatus());

        int passedTests = result.getCases() != null ? (int) result.getCases().stream()
                .filter(c -> "Accepted".equalsIgnoreCase(c.getStatus())).count() : 0;
        int totalTests = result.getCases() != null ? result.getCases().size() : 0;

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(request.getCode())
                .language(request.getLanguage() != null ? request.getLanguage() : "Java")
                .output(buildSubmissionOutput(result))
                .isAccepted(accepted)
                .executionStatus(result.getStatus())
                .executionTime(parseDouble(result.getRuntime()))
                .memoryUsage(parseInt(result.getMemory()))
                .problemTitle(problem.getTitle())
                .passedTests(passedTests)
                .totalTests(totalTests)
                .submittedAt(LocalDateTime.now())
                .build();

        log.info("====================");
        log.info("SUBMISSION SAVE START");
        log.info("USER ID: {}", userId);
        log.info("PROBLEM ID: {}", problem.getId());
        log.info("VERDICT: {}", submission.getExecutionStatus());
        log.info("====================");

        submission = submissionRepository.save(submission);

        log.info("====================");
        log.info("SUBMISSION SAVED");
        log.info("SUBMISSION ID: {}", submission.getId());
        log.info("====================");

        problem.updateStats(accepted);
        problemRepository.save(problem);

        if (accepted) {
            boolean isFirstAcceptance = !submissionRepository.existsByProblemIdAndUserIdAndIsAcceptedTrue(
                    problem.getId(), user.getId());
            if (isFirstAcceptance) {
                user.setProblemsSolved(user.getProblemsSolved() + 1);
                user.setLastSolvedDate(LocalDateTime.now());
                userRepository.save(user);
            }
        }

        return mapToDto(submission);
    }

    private String buildSubmissionOutput(CodeSubmissionResult result) {
        if (result == null || result.getCases() == null || result.getCases().isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.getCases().size(); i++) {
            CodeSubmissionCaseResult testCase = result.getCases().get(i);
            builder.append("[").append(i + 1).append("] ")
                    .append(testCase.getName()).append(" - ")
                    .append(testCase.getStatus()).append("\n");
            if (testCase.getOutput() != null && !testCase.getOutput().isEmpty()) {
                builder.append("Output: ").append(testCase.getOutput()).append("\n");
            }
            if (testCase.getExpected() != null && !testCase.getExpected().isEmpty()) {
                builder.append("Expected: ").append(testCase.getExpected()).append("\n");
            }
            if (i < result.getCases().size() - 1) {
                builder.append("---\n");
            }
        }
        return builder.toString();
    }

    private Double parseDouble(String value) {
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private Double parseInt(String value) {
        if (value == null) {
            return 0.0;
        }
        try {
            return (double) Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public SubmissionDto getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));
        return mapToDto(submission);
    }

    public Page<SubmissionDto> getUserSubmissions(Long userId, Pageable pageable) {
        try {
            System.out.println("SubmissionService.getUserSubmissions called with userId: " + userId);
            Page<Submission> submissions = submissionRepository.findByUserId(userId, pageable);
            System.out.println("Found " + submissions.getTotalElements() + " submissions from repository");
            
            Page<SubmissionDto> dtos = submissions.map(submission -> {
                System.out.println("Mapping submission id: " + submission.getId());
                try {
                    SubmissionDto dto = mapToDto(submission);
                    System.out.println("Successfully mapped submission id: " + submission.getId());
                    return dto;
                } catch (Exception e) {
                    System.out.println("ERROR mapping submission id: " + submission.getId());
                    System.out.println("Exception: " + e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
            
            System.out.println("Returning " + dtos.getContent().size() + " mapped DTOs");
            return dtos;
        } catch (Exception e) {
            System.out.println("EXCEPTION in getUserSubmissions: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<SubmissionDto> getProblemSubmissions(Long problemId, Long userId) {
        return submissionRepository.findByProblemIdAndUserId(problemId, userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private boolean simulateCodeExecution(String code) {
        // Simple simulation: if code contains "return" or "solve", mark as accepted
        return code.toLowerCase().contains("return") || code.toLowerCase().contains("solve");
    }

    private SubmissionDto mapToDto(Submission submission) {
        try {
            System.out.println("mapToDto: submission id=" + submission.getId());
            
            System.out.println("mapToDto: getting user...");
            User user = submission.getUser();
            System.out.println("mapToDto: user=" + (user != null ? user.getId() : "NULL"));
            
            System.out.println("mapToDto: getting problem...");
            Problem problem = submission.getProblem();
            System.out.println("mapToDto: problem=" + (problem != null ? problem.getId() : "NULL"));
            
            System.out.println("mapToDto: getting other fields...");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = submission.getSubmittedAt() != null ? submission.getSubmittedAt().format(formatter) : "NULL";
            System.out.println("mapToDto: submittedAt=" + formattedDate);
            
            SubmissionDto dto = SubmissionDto.builder()
                    .id(submission.getId())
                    .userId(user != null ? user.getId() : null)
                    .problemId(problem != null ? problem.getId() : null)
                    .problemTitle(submission.getProblemTitle())
                    .code(submission.getCode())
                    .output(submission.getOutput())
                    .isAccepted(submission.getIsAccepted())
                    .executionTime(submission.getExecutionTime())
                    .memoryUsage(submission.getMemoryUsage())
                    .language(submission.getLanguage())
                    .executionStatus(submission.getExecutionStatus())
                    .passedTests(submission.getPassedTests())
                    .totalTests(submission.getTotalTests())
                    .submittedAt(formattedDate)
                    .build();
            
            System.out.println("mapToDto: successfully built DTO for submission id=" + submission.getId());
            return dto;
        } catch (Exception e) {
            System.out.println("mapToDto: EXCEPTION - " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Double generateExecutionTime() {
        return 100.0 + Math.random() * 900.0;
    }

    private Double generateMemoryUsage() {
        return 128.0 + Math.random() * 9872.0;
    }

    public List<Long> getSolvedProblemIds(Long userId) {
        return submissionRepository.findByUserIdAndIsAcceptedTrueOrderBySubmittedAtDesc(userId, Pageable.unpaged())
                .stream()
                .map(s -> s.getProblem().getId())
                .distinct()
                .collect(Collectors.toList());
    }
}
