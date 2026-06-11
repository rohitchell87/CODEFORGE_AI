package com.codeforge.ai.service;

import com.codeforge.ai.dto.CreateProblemRequest;
import com.codeforge.ai.dto.ProblemDto;
import com.codeforge.ai.dto.ProblemExampleDto;
import com.codeforge.ai.dto.ProblemTestCaseDto;
import com.codeforge.ai.entity.Category;
import com.codeforge.ai.entity.Difficulty;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.entity.ProblemTag;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.ProblemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public Page<ProblemDto> getAllProblems(String difficulty, String tag, String search, Pageable pageable) {
        Difficulty diff = parseDifficulty(difficulty);
        String query = StringUtils.hasText(search) ? search.trim() : null;
        String normalizedTag = StringUtils.hasText(tag) ? tag.trim() : null;

        if (diff == null && normalizedTag == null && query == null) {
            return problemRepository.findAll(pageable).map(this::mapToDto);
        }

        return problemRepository.search(diff, normalizedTag, query, pageable).map(this::mapToDto);
    }

    public ProblemDto getProblemByIdentifier(String identifier) {
        if (StringUtils.hasText(identifier)) {
            Optional<Problem> bySlug = problemRepository.findBySlug(identifier);
            if (bySlug.isPresent()) {
                return mapToDto(bySlug.get());
            }
            try {
                Long id = Long.parseLong(identifier);
                return getProblemById(id);
            } catch (NumberFormatException ignored) {
                throw new ResourceNotFoundException("Problem not found with identifier: " + identifier);
            }
        }
        throw new ResourceNotFoundException("Problem identifier cannot be empty");
    }

    public ProblemDto getRandomProblem() {
        long count = problemRepository.count();
        if (count == 0) {
            throw new ResourceNotFoundException("No problems available");
        }
        int index = random.nextInt((int) count);
        return problemRepository.findAll(PageRequest.of(index, 1))
                .stream()
                .findFirst()
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No random problem could be selected"));
    }

    public List<ProblemDto> searchProblems(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        String normalizedQuery = query.trim();
        return problemRepository.search(null, null, normalizedQuery, PageRequest.of(0, 50))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProblemDto createProblem(CreateProblemRequest request) {
        Problem problem = buildProblemFromRequest(request);
        problem = problemRepository.save(problem);
        return mapToDto(problem);
    }

    public ProblemDto updateProblem(Long id, CreateProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        Problem updated = buildProblemFromRequest(request);

        problem.setTitle(updated.getTitle());
        problem.setSlug(updated.getSlug());
        problem.setDescription(updated.getDescription());
        problem.setDifficulty(updated.getDifficulty());
        problem.setCategory(updated.getCategory());
        problem.setExampleInput(updated.getExampleInput());
        problem.setExampleOutput(updated.getExampleOutput());
        problem.setSampleSolution(updated.getSampleSolution());
        problem.setConstraints(updated.getConstraints());
        problem.setHints(updated.getHints());
        problem.setStarterCodeJava(updated.getStarterCodeJava());
        problem.setStarterCodeCpp(updated.getStarterCodeCpp());
        problem.setStarterCodePython(updated.getStarterCodePython());
        problem.setVisibleTestCasesJson(updated.getVisibleTestCasesJson());
        problem.setHiddenTestCasesJson(updated.getHiddenTestCasesJson());
        problem.setSolutionTemplate(updated.getSolutionTemplate());
        problem.setOrderIndex(updated.getOrderIndex());
        problem.setTags(updated.getTags());

        problem = problemRepository.save(problem);
        return mapToDto(problem);
    }

    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        problemRepository.delete(problem);
    }

    private Problem buildProblemFromRequest(CreateProblemRequest request) {
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .slug(slugify(request.getSlug() != null ? request.getSlug() : request.getTitle()))
                .description(request.getDescription())
                .difficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()))
                .category(Category.valueOf(request.getCategory().toUpperCase()))
                .exampleInput(request.getExamples() != null && !request.getExamples().isEmpty() ? request.getExamples().get(0).getInput() : null)
                .exampleOutput(request.getExamples() != null && !request.getExamples().isEmpty() ? request.getExamples().get(0).getOutput() : null)
                .sampleSolution(request.getSampleSolution())
                .constraints(request.getConstraints() == null ? null : String.join("\n", request.getConstraints()))
                .hints(request.getHints() == null ? null : String.join("\n", request.getHints()))
                .starterCodeJava(request.getStarterCodeJava())
                .starterCodeCpp(request.getStarterCodeCpp())
                .starterCodePython(request.getStarterCodePython())
                .visibleTestCasesJson(serializeTestCases(request.getVisibleTestCases()))
                .hiddenTestCasesJson(serializeTestCases(request.getHiddenTestCases()))
                .solutionTemplate(request.getSolutionTemplate())
                .orderIndex(request.getOrderIndex())
                .build();

        problem.setTags(buildTags(problem, request.getTags()));
        return problem;
    }

    private Difficulty parseDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return null;
        }
        return Difficulty.valueOf(difficulty.toUpperCase());
    }

    private String serializeTestCases(List<ProblemTestCaseDto> testCases) {
        if (testCases == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(testCases);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<ProblemTestCaseDto> parseTestCases(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ProblemTestCaseDto>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String slugify(String title) {
        if (!StringUtils.hasText(title)) {
            return "problem" + System.currentTimeMillis();
        }
        return title.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private Set<ProblemTag> buildTags(Problem problem, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(tag -> ProblemTag.builder()
                        .problem(problem)
                        .tag(tag)
                        .build())
                .collect(Collectors.toSet());
    }

    private ProblemDto mapToDto(Problem problem) {
        return ProblemDto.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .slug(problem.getSlug())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty().toString())
                .category(problem.getCategory().toString())
                .exampleInput(problem.getExampleInput())
                .exampleOutput(problem.getExampleOutput())
                .examples(parseExamples(problem.getExampleInput(), problem.getExampleOutput()))
                .sampleSolution(problem.getSampleSolution())
                .constraints(problem.getConstraints() == null ? List.of() : List.of(problem.getConstraints().split("\\n")))
                .hints(problem.getHints() == null ? List.of() : List.of(problem.getHints().split("\\n")))
                .starterCodeJava(problem.getStarterCodeJava())
                .starterCodeCpp(problem.getStarterCodeCpp())
                .starterCodePython(problem.getStarterCodePython())
                .visibleTestCases(parseTestCases(problem.getVisibleTestCasesJson()))
                .hiddenTestCases(parseTestCases(problem.getHiddenTestCasesJson()))
                .solutionTemplate(problem.getSolutionTemplate())
                .orderIndex(problem.getOrderIndex())
                .acceptanceRate(problem.getAcceptanceRate())
                .submissionCount(problem.getSubmissionCount())
                .acceptedCount(problem.getAcceptedCount())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .tags(problem.getTags() == null ? List.of() : problem.getTags().stream()
                        .map(ProblemTag::getTag)
                        .distinct()
                        .collect(Collectors.toList()))
                .build();
    }

    private List<ProblemExampleDto> parseExamples(String input, String output) {
        if (!StringUtils.hasText(input) && !StringUtils.hasText(output)) {
            return List.of();
        }
        return List.of(ProblemExampleDto.builder()
                .input(input == null ? "" : input)
                .output(output == null ? "" : output)
                .explanation("")
                .build());
    }

    public ProblemDto getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        return mapToDto(problem);
    }
}
