package com.codeforge.ai;

import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.entity.ProblemTag;
import com.codeforge.ai.repository.ProblemRepository;
import com.codeforge.ai.seed.ProblemSeed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class ProblemDataLoader implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final ObjectMapper objectMapper;

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

        if (existing > 0) {
            log.info("Existing problems found. Patching missing problems from seed data if necessary.");
            patchExistingProblems(seeds);
            return;
        }

        log.info("Parsed {} problem seeds from JSON", seeds.size());
        for (ProblemSeed seed : seeds) {
            Problem problem = buildProblem(seed);
            problemRepository.save(problem);
        }

        log.info("Finished seeding {} problems into the database", seeds.size());
    }

    private void patchExistingProblems(List<ProblemSeed> seeds) {
        for (ProblemSeed seed : seeds) {
            String slug = generateSlug(seed.getSlug(), seed.getTitle());
            problemRepository.findBySlug(slug).ifPresent(problem -> {
                boolean updated = false;

                String visibleJson = writeJson(seed.getVisibleTestCases());
                if ((problem.getVisibleTestCasesJson() == null || problem.getVisibleTestCasesJson().isBlank()) && visibleJson != null) {
                    problem.setVisibleTestCasesJson(visibleJson);
                    updated = true;
                }

                String hiddenJson = writeJson(seed.getHiddenTestCases());
                if ((problem.getHiddenTestCasesJson() == null || problem.getHiddenTestCasesJson().isBlank()) && hiddenJson != null) {
                    problem.setHiddenTestCasesJson(hiddenJson);
                    updated = true;
                }

                if ((problem.getExampleInput() == null || problem.getExampleInput().isBlank()) && seed.getExamples() != null && !seed.getExamples().isEmpty()) {
                    problem.setExampleInput(seed.getExamples().get(0).getInput());
                    updated = true;
                }
                if ((problem.getExampleOutput() == null || problem.getExampleOutput().isBlank()) && seed.getExamples() != null && !seed.getExamples().isEmpty()) {
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
