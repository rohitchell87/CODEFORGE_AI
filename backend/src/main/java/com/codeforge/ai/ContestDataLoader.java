package com.codeforge.ai;

import com.codeforge.ai.entity.Contest;
import com.codeforge.ai.entity.ContestProblem;
import com.codeforge.ai.entity.Problem;
import com.codeforge.ai.repository.ContestRepository;
import com.codeforge.ai.repository.ProblemRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
@Order(2)
public class ContestDataLoader implements CommandLineRunner {
    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Problem> problems = problemRepository.findAll();
        if (problems.size() < 4) {
            log.warn("Not enough problems available to seed contests. Found {} problems.", problems.size());
            return;
        }

        log.info("Seeding practice contests using {} problems", problems.size());

        final Contest contestOne = buildContest(
                "CodeForge Practice Sprint",
                "A short practice contest with algorithmic problems for fast-paced coding.",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(3),
                180,
                selectProblems(problems, 0)
        );

        final Contest contestTwo = buildContest(
                "AI Problem Solving Challenge",
                "Tackle a set of curated problems focused on AI and data structures.",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(4),
                240,
                selectProblems(problems, 4)
        );

        final Contest contestThree = buildContest(
                "Interview Preparation Contest",
                "A curated contest covering arrays, hashing, stacks, linked lists, trees, and dynamic programming.",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(3).plusMinutes(30),
                210,
                selectProblems(problems, 8)
        );

        saveContestIfMissing(contestOne);
        saveContestIfMissing(contestTwo);
        saveContestIfMissing(contestThree);

        log.info("Contest seeding complete, contest count is now {}", contestRepository.count());
    }

    private void saveContestIfMissing(Contest contest) {
        if (contestRepository.existsByTitle(contest.getTitle())) {
            log.info("Contest '{}' already exists. Skipping insert.", contest.getTitle());
            return;
        }
        contestRepository.save(Objects.requireNonNull(contest));
        log.info("Created contest '{}'.", contest.getTitle());
    }

    private List<Problem> selectProblems(List<Problem> problems, int startIndex) {
        if (problems.size() >= startIndex + 4) {
            return problems.subList(startIndex, startIndex + 4);
        }
        int fallbackStart = Math.max(0, problems.size() - 4);
        return problems.subList(fallbackStart, problems.size());
    }

    private Contest buildContest(String title, String description, LocalDateTime startTime, LocalDateTime endTime, Integer durationInMinutes, List<Problem> problems) {
        Contest contest = Contest.builder()
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .durationInMinutes(durationInMinutes)
                .build();

        Set<ContestProblem> contestProblems = new LinkedHashSet<>();
        int index = 1;
        for (Problem problem : problems) {
            ContestProblem contestProblem = ContestProblem.builder()
                    .contest(contest)
                    .problem(problem)
                    .orderIndex(index)
                    .points(100)
                    .build();
            contestProblems.add(contestProblem);
            index++;
        }
        contest.setProblems(contestProblems);
        return contest;
    }
}
