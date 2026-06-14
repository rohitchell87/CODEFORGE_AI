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
public class UserProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Integer problemsSolved;
    private Integer currentStreak;
    private Integer maxStreak;
    private String role;
    private LocalDateTime createdAt;
}
