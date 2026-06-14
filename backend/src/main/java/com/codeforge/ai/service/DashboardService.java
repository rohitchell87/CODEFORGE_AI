package com.codeforge.ai.service;

import com.codeforge.ai.dto.DashboardActivityDto;
import com.codeforge.ai.dto.DashboardStatsDto;
import com.codeforge.ai.dto.DashboardTrendDto;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.SubmissionRepository;
import com.codeforge.ai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class DashboardService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    public DashboardStatsDto getDashboardOverview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        long acceptedCount = submissionRepository.countByUserIdAndIsAcceptedTrue(userId);
        long totalSubmissions = submissionRepository.countByUserId(userId);
        int accuracy = totalSubmissions == 0 ? 0 : (int) Math.round((acceptedCount * 100.0) / totalSubmissions);
        int contests = user.getContestParticipations() == null ? 0 : user.getContestParticipations().size();
        int solvedCount = (int) submissionRepository.findByUserIdAndIsAcceptedTrueOrderBySubmittedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(submission -> submission.getProblem().getId())
                .distinct()
                .count();

        List<DashboardActivityDto> recentActivity = List.of(
                DashboardActivityDto.builder().label("Solved").value(solvedCount).build(),
                DashboardActivityDto.builder().label("Accepted").value((int) acceptedCount).build(),
                DashboardActivityDto.builder().label("Attempts").value((int) totalSubmissions).build()
        );

        List<DashboardTrendDto> solvedTrend = buildSolvedTrend(userId);

        return DashboardStatsDto.builder()
                .solvedCount(solvedCount)
                .totalSubmissions((int) totalSubmissions)
                .streak(user.getCurrentStreak() == null ? 0 : user.getCurrentStreak())
                .accuracy(accuracy)
                .contests(contests)
                .recentActivity(recentActivity)
                .solvedTrend(solvedTrend)
                .build();
    }

    private List<DashboardTrendDto> buildSolvedTrend(Long userId) {
        List<DashboardTrendDto> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");

        for (int offset = 29; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            long count = submissionRepository.countByUserIdAndIsAcceptedTrueAndSubmittedAtBetween(userId, start, end);
            trend.add(DashboardTrendDto.builder()
                    .day(date.format(formatter))
                    .solved((int) count)
                    .build());
        }

        return trend;
    }
}
