package com.codeforge.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(columnDefinition = "LONGTEXT")
    private String code;

    @Column(columnDefinition = "LONGTEXT")
    private String output;

    private Boolean isAccepted;

    private Double executionTime;

    private Double memoryUsage;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private String language;

    private String executionStatus;

    private Integer passedTests;

    private Integer totalTests;

    private String problemTitle;
}
