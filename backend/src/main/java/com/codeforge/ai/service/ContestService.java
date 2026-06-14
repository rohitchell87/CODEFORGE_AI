package com.codeforge.ai.service;

import com.codeforge.ai.dto.ContestDto;
import com.codeforge.ai.dto.ContestParticipationDto;
import com.codeforge.ai.dto.ContestProblemDto;
import com.codeforge.ai.entity.Contest;
import com.codeforge.ai.entity.ContestParticipation;
import com.codeforge.ai.entity.ContestProblem;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.InvalidInputException;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.ContestParticipationRepository;
import com.codeforge.ai.repository.ContestRepository;
import com.codeforge.ai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ContestService {

    private ContestRepository contestRepository;
    private ContestParticipationRepository participationRepository;
    private UserRepository userRepository;

    public Page<ContestDto> getAllContests(Pageable pageable) {
        return contestRepository.findAll(pageable).map(this::mapContestToDto);
    }

    public ContestDto getContestById(@NonNull Long id) {
        Contest contest = contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
        return mapContestToDto(contest);
    }

    public void participateInContest(@NonNull Long userId, @NonNull Long contestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));

        if (contest.getEndTime().isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("Contest has already ended");
        }

        Optional<ContestParticipation> existing = participationRepository.findByContestIdAndUserId(contestId, userId);

        if (existing.isEmpty()) {
            final ContestParticipation participation = ContestParticipation.builder()
                    .user(user)
                    .contest(contest)
                    .score(0)
                    .rank(0)
                    .problemsSolved(0)
                    .build();

            participationRepository.save(Objects.requireNonNull(participation));
        }
    }

    public void leaveContest(@NonNull Long userId, @NonNull Long contestId) {
        ContestParticipation participation = participationRepository.findByContestIdAndUserId(contestId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found"));

        participation.setLeftAt(LocalDateTime.now());
        participationRepository.save(participation);
    }

    public Page<ContestParticipationDto> getContestLeaderboard(Long contestId, Pageable pageable) {
        return participationRepository.findByContestIdOrderByScoreDescRankAsc(contestId, pageable)
                .map(this::mapParticipationToDto);
    }

    public List<ContestParticipationDto> getUserContests(Long userId) {
        return participationRepository.findByUserIdOrderByJoinedAtDesc(userId).stream()
                .map(this::mapParticipationToDto)
                .collect(Collectors.toList());
    }

    private ContestDto mapContestToDto(Contest contest) {
        List<ContestProblemDto> problems = contest.getProblems().stream()
                .sorted(Comparator.comparingInt(p -> p.getOrderIndex() == null ? 0 : p.getOrderIndex()))
                .map(this::mapContestProblemToDto)
                .collect(Collectors.toList());

        return ContestDto.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .durationInMinutes(contest.getDurationInMinutes())
                .status(getContestStatus(contest))
                .problemCount(contest.getProblems().size())
                .participantCount(contest.getParticipations().size())
                .problems(problems)
                .build();
    }

    private ContestProblemDto mapContestProblemToDto(ContestProblem contestProblem) {
        return ContestProblemDto.builder()
                .id(contestProblem.getId())
                .problemId(contestProblem.getProblem().getId())
                .title(contestProblem.getProblem().getTitle())
                .difficulty(contestProblem.getProblem().getDifficulty() != null ? contestProblem.getProblem().getDifficulty().name() : null)
                .category(contestProblem.getProblem().getCategory() != null ? contestProblem.getProblem().getCategory().name() : null)
                .orderIndex(contestProblem.getOrderIndex())
                .points(contestProblem.getPoints())
                .build();
    }

    private ContestParticipationDto mapParticipationToDto(ContestParticipation participation) {
        return ContestParticipationDto.builder()
                .id(participation.getId())
                .contestId(participation.getContest().getId())
                .userId(participation.getUser().getId())
                .userEmail(participation.getUser().getEmail())
                .userFirstName(participation.getUser().getFirstName())
                .userLastName(participation.getUser().getLastName())
                .score(participation.getScore())
                .rank(participation.getRank())
                .problemsSolved(participation.getProblemsSolved())
                .joinedAt(participation.getJoinedAt())
                .leftAt(participation.getLeftAt())
                .build();
    }

    private String getContestStatus(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime())) {
            return "Upcoming";
        }
        if (!now.isAfter(contest.getEndTime())) {
            return "Active";
        }
        return "Finished";
    }
}
