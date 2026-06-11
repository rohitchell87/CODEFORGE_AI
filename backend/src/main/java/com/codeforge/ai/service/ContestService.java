package com.codeforge.ai.service;

import com.codeforge.ai.entity.Contest;
import com.codeforge.ai.entity.ContestParticipation;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.ContestParticipationRepository;
import com.codeforge.ai.repository.ContestRepository;
import com.codeforge.ai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ContestService {

    private ContestRepository contestRepository;
    private ContestParticipationRepository participationRepository;
    private UserRepository userRepository;

    public Page<Contest> getAllContests(Pageable pageable) {
        return contestRepository.findAll(pageable);
    }

    public Contest getContestById(Long id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));
    }

    public void participateInContest(Long userId, Long contestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));

        Optional<ContestParticipation> existing = participationRepository.findByContestIdAndUserId(contestId, userId);

        if (existing.isEmpty()) {
            ContestParticipation participation = ContestParticipation.builder()
                    .user(user)
                    .contest(contest)
                    .build();

            participationRepository.save(participation);
        }
    }

    public void leaveContest(Long userId, Long contestId) {
        ContestParticipation participation = participationRepository.findByContestIdAndUserId(contestId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found"));

        participation.setLeftAt(LocalDateTime.now());
        participationRepository.save(participation);
    }

    public Page<ContestParticipation> getContestLeaderboard(Long contestId, Pageable pageable) {
        return participationRepository.findByContestId(contestId, pageable);
    }

    public List<ContestParticipation> getUserContests(Long userId) {
        return participationRepository.findByUserIdOrderByJoinedAtDesc(userId);
    }
}
