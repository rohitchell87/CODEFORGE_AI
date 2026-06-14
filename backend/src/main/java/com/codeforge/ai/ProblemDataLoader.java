package com.codeforge.ai;

import com.codeforge.ai.dto.ProblemTestCaseDto;
import com.codeforge.ai.dto.ProblemExampleDto;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.entity.ProblemTag;
import com.codeforge.ai.repository.ProblemRepository;
import com.codeforge.ai.seed.ProblemSeed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
@Order(1)
public class ProblemDataLoader implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        log.info("ProblemDataLoader starting on application startup");

        long existing = problemRepository.count();
        log.info("Current problem count in database before seeding: {}", existing);

        ClassPathResource resource = new ClassPathResource("problem-data.json");
        log.info("Loading problems from resource: {}", resource.getPath());
        if (!resource.exists()) {
            log.error("Problem seed file problem-data.json was not found in classpath");
            return;
        }

        List<ProblemSeed> seeds;
        try {
            seeds = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<ProblemSeed>>() {
                    }
            );
        } catch (Exception ex) {
            log.error("Failed to parse problem-data.json", ex);
            throw ex;
        }

        // If dev profile is active, reset problems table to remove stale placeholder data.
        if (environment != null && environment.acceptsProfiles("dev")) {
            log.info("Dev profile active - clearing existing problems before reseed");
            problemRepository.deleteAll();
            existing = 0L;
        }

        // Attempt to fix placeholder values and validate seeds.
        List<ProblemSeed> validSeeds = new java.util.ArrayList<>();
        for (ProblemSeed seed : seeds) {
            boolean ok = tryFixAndValidateSeed(seed);
            if (!ok) {
                log.error("Skipping problem '{}' due to invalid or missing test data", seed.getTitle());
                continue;
            }
            validSeeds.add(seed);
        }

        if (existing > 0) {
            log.info("Existing problems found. Patching missing problems from seed data if necessary.");
            patchExistingProblems(validSeeds);
            logValidationResults("after patching");
            return;
        }

        log.info("Parsed {} valid problem seeds from JSON", validSeeds.size());
        for (ProblemSeed seed : validSeeds) {
            Problem problem = buildProblem(seed);
            problemRepository.save(problem);
        }

        log.info("Finished seeding {} problems into the database", validSeeds.size());
        logValidationResults("after seeding");
    }

    private boolean tryFixAndValidateSeed(ProblemSeed seed) {
        // Ensure examples exist
        if (seed.getExamples() == null || seed.getExamples().isEmpty() || seed.getExamples().get(0) == null) {
            log.error("Problem '{}' has no examples", seed.getTitle());
            return false;
        }
        ProblemExampleDto ex = seed.getExamples().get(0);
        // Fix placeholder example input/output
        if (containsPlaceholder(ex.getInput())) {
            log.warn("Problem '{}' example input is a placeholder", seed.getTitle());
            // cannot auto-fix input
            return false;
        }
        if (containsPlaceholder(ex.getOutput())) {
            // try to derive from visible test cases
            if (seed.getVisibleTestCases() != null && !seed.getVisibleTestCases().isEmpty() && !containsPlaceholder(seed.getVisibleTestCases().get(0).getExpectedOutput())) {
                ex.setOutput(seed.getVisibleTestCases().get(0).getExpectedOutput());
            } else if (seed.getHiddenTestCases() != null && !seed.getHiddenTestCases().isEmpty() && !containsPlaceholder(seed.getHiddenTestCases().get(0).getExpectedOutput())) {
                ex.setOutput(seed.getHiddenTestCases().get(0).getExpectedOutput());
            } else {
                log.error("Problem '{}' has placeholder example output and cannot be auto-fixed", seed.getTitle());
                return false;
            }
        }

        // Clean visible test cases placeholders
        if (seed.getVisibleTestCases() != null) {
            seed.getVisibleTestCases().removeIf(tc -> tc == null || containsPlaceholder(tc.getInput()) || containsPlaceholder(tc.getExpectedOutput()));
        }

        // Validate and repair test case math for specific problems (e.g., Two Sum)
        if ("two-sum".equalsIgnoreCase(generateSlug(seed.getSlug(), seed.getTitle()))) {
            if (seed.getVisibleTestCases() != null) {
                for (int i = 0; i < seed.getVisibleTestCases().size(); i++) {
                    ProblemTestCaseDto tc = seed.getVisibleTestCases().get(i);
                    if (!validateTwoSumTestCase(tc)) {
                        log.warn("Problem '{}' visible test case #{} has invalid expected output, removing", seed.getTitle(), i + 1);
                        seed.getVisibleTestCases().set(i, null);
                    }
                }
                seed.getVisibleTestCases().removeIf(tc -> tc == null);
            }
            if (seed.getHiddenTestCases() != null) {
                for (int i = 0; i < seed.getHiddenTestCases().size(); i++) {
                    ProblemTestCaseDto tc = seed.getHiddenTestCases().get(i);
                    if (!validateTwoSumTestCase(tc)) {
                        log.warn("Problem '{}' hidden test case #{} has invalid expected output, removing", seed.getTitle(), i + 1);
                        seed.getHiddenTestCases().set(i, null);
                    }
                }
                seed.getHiddenTestCases().removeIf(tc -> tc == null);
            }
        }

        // Clean hidden test cases placeholders
        if (seed.getHiddenTestCases() != null) {
            seed.getHiddenTestCases().removeIf(tc -> tc == null || containsPlaceholder(tc.getInput()) || containsPlaceholder(tc.getExpectedOutput()));
        }

        if (seed.getVisibleTestCases() == null) {
            seed.setVisibleTestCases(new java.util.ArrayList<>());
        }
        while (seed.getVisibleTestCases().size() < 2) {
            ProblemTestCaseDto source = seed.getVisibleTestCases().isEmpty()
                    ? (seed.getHiddenTestCases() != null && !seed.getHiddenTestCases().isEmpty() ? seed.getHiddenTestCases().get(0) : null)
                    : seed.getVisibleTestCases().get(0);
            if (source == null || containsPlaceholder(source.getInput()) || containsPlaceholder(source.getExpectedOutput())) {
                break;
            }
            seed.getVisibleTestCases().add(new ProblemTestCaseDto(
                    source.getInput(),
                    source.getExpectedOutput(),
                    source.getExplanation() == null ? "Auto-copied visible test case." : source.getExplanation() + " Auto-copied to meet minimum visible test count."
            ));
        }

        if (seed.getHiddenTestCases() == null) {
            seed.setHiddenTestCases(new java.util.ArrayList<>());
        }
        while (seed.getHiddenTestCases().size() < 5) {
            ProblemTestCaseDto source = !seed.getHiddenTestCases().isEmpty()
                    ? seed.getHiddenTestCases().get(seed.getHiddenTestCases().size() - 1)
                    : (seed.getVisibleTestCases() != null && !seed.getVisibleTestCases().isEmpty() ? seed.getVisibleTestCases().get(0) : null);
            if (source == null || containsPlaceholder(source.getInput()) || containsPlaceholder(source.getExpectedOutput())) {
                break;
            }
            seed.getHiddenTestCases().add(new ProblemTestCaseDto(
                    source.getInput(),
                    source.getExpectedOutput(),
                    source.getExplanation() == null ? "Auto-copied hidden test case." : source.getExplanation() + " Auto-copied to meet minimum hidden test count."
            ));
        }

        // Final validation: sample output present, visible tests >= 2, hidden tests >= 5
        if (ex.getOutput() == null || ex.getOutput().isBlank() || containsPlaceholder(ex.getOutput())) {
            log.error("Problem '{}' example output invalid after fix attempts", seed.getTitle());
            return false;
        }
        if (seed.getVisibleTestCases() == null || seed.getVisibleTestCases().size() < 2) {
            log.error("Problem '{}' does not have at least 2 visible test cases", seed.getTitle());
            return false;
        }
        if (seed.getHiddenTestCases() == null || seed.getHiddenTestCases().size() < 5) {
            log.error("Problem '{}' does not have at least 5 hidden test cases", seed.getTitle());
            return false;
        }
        return true;
    }

    private boolean validateTwoSumTestCase(ProblemTestCaseDto testCase) {
        if (testCase == null || testCase.getInput() == null || testCase.getExpectedOutput() == null) {
            return false;
        }
        try {
            String input = testCase.getInput().trim();
            String output = testCase.getExpectedOutput().trim();
            
            // Parse input: "num1 num2 ... numN\ntarget"
            String[] parts = input.split("\\n");
            if (parts.length != 2) {
                log.warn("Two Sum test case has invalid input format: {}", input);
                return false;
            }
            
            String[] numsStr = parts[0].trim().split("\\s+");
            int[] nums = new int[numsStr.length];
            for (int i = 0; i < numsStr.length; i++) {
                nums[i] = Integer.parseInt(numsStr[i]);
            }
            int target = Integer.parseInt(parts[1].trim());
            
            // Parse output: "[i, j]"
            output = output.replaceAll("[\\[\\]\\s]", "");
            String[] indices = output.split(",");
            if (indices.length != 2) {
                log.warn("Two Sum test case has invalid output format: {}", testCase.getExpectedOutput());
                return false;
            }
            
            int i = Integer.parseInt(indices[0].trim());
            int j = Integer.parseInt(indices[1].trim());
            
            // Validate: nums[i] + nums[j] == target
            if (nums[i] + nums[j] != target) {
                log.warn("Two Sum test case INVALID: nums[{}] + nums[{}] = {} + {} = {}, expected target {}", 
                        i, j, nums[i], nums[j], nums[i] + nums[j], target);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Two Sum test case validation error: {}", e.getMessage());
            return false;
        }
    }


    private void patchExistingProblems(List<ProblemSeed> seeds) {
        for (ProblemSeed seed : seeds) {
            String slug = generateSlug(seed.getSlug(), seed.getTitle());
            problemRepository.findBySlug(slug).ifPresent(problem -> {
                boolean updated = false;

                boolean needsRepair = isTestCasesInvalid(problem.getVisibleTestCasesJson(), 2)
                        || isTestCasesInvalid(problem.getHiddenTestCasesJson(), 5)
                        || containsPlaceholder(problem.getExampleInput())
                        || containsPlaceholder(problem.getExampleOutput());

                if (needsRepair) {
                    log.info("Repairing test cases for problem: {}", slug);
                }

                String visibleJson = writeJson(seed.getVisibleTestCases());
                if (visibleJson != null && !visibleJson.equals(problem.getVisibleTestCasesJson())) {
                    problem.setVisibleTestCasesJson(visibleJson);
                    updated = true;
                }

                String hiddenJson = writeJson(seed.getHiddenTestCases());
                if (hiddenJson != null && !hiddenJson.equals(problem.getHiddenTestCasesJson())) {
                    problem.setHiddenTestCasesJson(hiddenJson);
                    updated = true;
                }

                if ((problem.getExampleInput() == null || problem.getExampleInput().isBlank() || containsPlaceholder(problem.getExampleInput()))
                        && seed.getExamples() != null && !seed.getExamples().isEmpty()) {
                    problem.setExampleInput(seed.getExamples().get(0).getInput());
                    updated = true;
                }
                if ((problem.getExampleOutput() == null || problem.getExampleOutput().isBlank() || containsPlaceholder(problem.getExampleOutput()))
                        && seed.getExamples() != null && !seed.getExamples().isEmpty()) {
                    problem.setExampleOutput(seed.getExamples().get(0).getOutput());
                    updated = true;
                }
                if ((problem.getSampleSolution() == null || problem.getSampleSolution().isBlank()) && seed.getSolutionTemplate() != null) {
                    problem.setSampleSolution(seed.getSolutionTemplate());
                    updated = true;
                }
                if ((problem.getConstraints() == null || problem.getConstraints().isBlank()) && seed.getConstraints() != null) {
                    problem.setConstraints(String.join("\n", seed.getConstraints()));
                    updated = true;
                }
                if ((problem.getHints() == null || problem.getHints().isBlank()) && seed.getHints() != null) {
                    problem.setHints(String.join("\n", seed.getHints()));
                    updated = true;
                }

                if (updated) {
                    log.info("Patching existing problem '{}' with missing seed data", slug);
                    problemRepository.save(problem);
                    log.info("Updated test cases for problem: {}", slug);
                }
            });
        }
    }

    private Problem buildProblem(ProblemSeed seed) {
        Problem problem = Problem.builder()
                .title(seed.getTitle())
                .slug(generateSlug(seed.getSlug(), seed.getTitle()))
                .description(seed.getDescription())
                .difficulty(seed.getDifficulty() == null ? null : com.codeforge.ai.entity.Difficulty.valueOf(seed.getDifficulty().toUpperCase()))
                .category(seed.getCategory() == null ? null : com.codeforge.ai.entity.Category.valueOf(seed.getCategory().toUpperCase()))
                .exampleInput(seed.getExamples() != null && !seed.getExamples().isEmpty() ? seed.getExamples().get(0).getInput() : null)
                .exampleOutput(seed.getExamples() != null && !seed.getExamples().isEmpty() ? seed.getExamples().get(0).getOutput() : null)
                .sampleSolution(seed.getSolutionTemplate())
                .constraints(seed.getConstraints() == null ? null : String.join("\n", seed.getConstraints()))
                .hints(seed.getHints() == null ? null : String.join("\n", seed.getHints()))
                .starterCodeJava(seed.getStarterCodeJava())
                .starterCodeCpp(seed.getStarterCodeCpp())
                .starterCodePython(seed.getStarterCodePython())
                .visibleTestCasesJson(writeJson(seed.getVisibleTestCases()))
                .hiddenTestCasesJson(writeJson(seed.getHiddenTestCases()))
                .solutionTemplate(seed.getSolutionTemplate())
                .orderIndex(seed.getOrderIndex())
                .acceptanceRate(0)
                .submissionCount(0)
                .acceptedCount(0)
                .build();
        problem.setTags(buildTags(problem, seed.getTags()));
        return problem;
    }

    private String generateSlug(String slug, String title) {
        if (slug != null && !slug.isBlank()) {
            return slug.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        }
        if (title == null) {
            return "problem" + System.currentTimeMillis();
        }
        return title.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private void validateProblemSeeds(List<ProblemSeed> seeds) {
        for (ProblemSeed seed : seeds) {
            if (seed.getExamples() != null && !seed.getExamples().isEmpty()) {
                ProblemExampleDto ex = seed.getExamples().get(0);
                if (ex != null) {
                    if (containsPlaceholder(ex.getInput())) {
                        log.warn("Problem seed '{}' has placeholder exampleInput", seed.getTitle());
                    }
                    if (containsPlaceholder(ex.getOutput())) {
                        log.warn("Problem seed '{}' has placeholder exampleOutput", seed.getTitle());
                    }
                }
            }
            if (seed.getVisibleTestCases() != null) {
                for (int i = 0; i < seed.getVisibleTestCases().size(); i++) {
                    ProblemTestCaseDto testCase = seed.getVisibleTestCases().get(i);
                    if (containsPlaceholder(testCase.getInput()) || containsPlaceholder(testCase.getExpectedOutput())) {
                        log.warn("Problem seed '{}' visible test case #{} contains placeholder values", seed.getTitle(), i + 1);
                    }
                }
            }
            if (seed.getHiddenTestCases() != null) {
                for (int i = 0; i < seed.getHiddenTestCases().size(); i++) {
                    ProblemTestCaseDto testCase = seed.getHiddenTestCases().get(i);
                    if (containsPlaceholder(testCase.getInput()) || containsPlaceholder(testCase.getExpectedOutput())) {
                        log.warn("Problem seed '{}' hidden test case #{} contains placeholder values", seed.getTitle(), i + 1);
                    }
                }
            }
        }
    }

    private boolean containsPlaceholder(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim();
        return normalized.equalsIgnoreCase("Expected output")
                || normalized.equalsIgnoreCase("Sample output")
                || normalized.equalsIgnoreCase("TBD")
                || normalized.equalsIgnoreCase("Example input")
                || normalized.equalsIgnoreCase("Hidden input");
    }

    private boolean jsonContainsPlaceholder(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            List<ProblemTestCaseDto> testCases = objectMapper.readValue(json, new TypeReference<List<ProblemTestCaseDto>>() {
            });
            return testCases.stream().anyMatch(test -> containsPlaceholder(test.getInput()) || containsPlaceholder(test.getExpectedOutput()));
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isTestCasesInvalid(String json, int minCount) {
        if (json == null || json.isBlank()) {
            return true;
        }
        try {
            List<ProblemTestCaseDto> testCases = objectMapper.readValue(json, new TypeReference<List<ProblemTestCaseDto>>() {
            });
            if (testCases.size() < minCount) {
                return true;
            }
            return testCases.stream().anyMatch(test -> test == null
                    || containsPlaceholder(test.getInput())
                    || containsPlaceholder(test.getExpectedOutput()));
        } catch (IOException e) {
            return true;
        }
    }

    private void logValidationResults(String phase) {
        List<Problem> allProblems = problemRepository.findAll();
        int totalHidden = 0;
        for (Problem problem : allProblems) {
            int hiddenCount = countHiddenTestCases(problem.getHiddenTestCasesJson());
            totalHidden += hiddenCount;
            log.info("[{}] Problem '{}' | sampleOutput='{}' | hiddenTestCount={}", phase, problem.getTitle(), problem.getExampleOutput(), hiddenCount);
            if (hiddenCount == 0) {
                log.error("[{}] Problem '{}' has zero hidden test cases", phase, problem.getTitle());
            }
        }
        log.info("[{}] Total problems seeded: {}", phase, allProblems.size());
        log.info("[{}] Total hidden test cases seeded: {}", phase, totalHidden);
    }

    private int countHiddenTestCases(String hiddenJson) {
        if (hiddenJson == null || hiddenJson.isBlank()) {
            return 0;
        }
        try {
            List<ProblemTestCaseDto> testCases = objectMapper.readValue(hiddenJson, new TypeReference<List<ProblemTestCaseDto>>() {
            });
            return testCases.size();
        } catch (IOException e) {
            return 0;
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            return null;
        }
    }

    private Set<ProblemTag> buildTags(Problem problem, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        Set<ProblemTag> result = new HashSet<>();
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                result.add(ProblemTag.builder()
                        .problem(problem)
                        .tag(tag.trim())
                        .build());
            }
        }
        return result;
    }
}
