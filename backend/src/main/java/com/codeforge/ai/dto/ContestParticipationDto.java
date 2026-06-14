package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContestParticipationDto {
    private Long id;
    private Long contestId;
    private Long userId;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private Integer score;
    private Integer rank;
    private Integer problemsSolved;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
