package com.codeforge.ai.service;

import com.codeforge.ai.dto.UserProfileDto;
import com.codeforge.ai.entity.User;
import com.codeforge.ai.exception.ResourceNotFoundException;
import com.codeforge.ai.repository.SubmissionRepository;
import com.codeforge.ai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

    private UserRepository userRepository;
    private SubmissionRepository submissionRepository;

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToProfileDto(user);
    }

    public UserProfileDto getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return mapToProfileDto(user);
    }

    public void updateUserStreak(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update streak logic - simplified
        user.setLastSolvedDate(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    public long getUserAcceptedSubmissionsCount(Long userId) {
        return submissionRepository.countByUserIdAndIsAcceptedTrue(userId);
    }

    public List<Long> getSolvedProblems(Long userId) {
        return submissionRepository.findByUserIdAndIsAcceptedTrueOrderBySubmittedAtDesc(userId, Pageable.unpaged())
                .stream()
                .map(submission -> submission.getProblem().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    private UserProfileDto mapToProfileDto(User user) {
        long acceptedSubmissions = submissionRepository.countByUserIdAndIsAcceptedTrue(user.getId());
        
        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .problemsSolved((int) acceptedSubmissions)
                .currentStreak(user.getCurrentStreak())
                .maxStreak(user.getMaxStreak())
                .role(user.getRole().toString())
                .build();
    }
}
