package com.codeforge.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "problems", indexes = {
        @Index(name = "idx_problem_slug", columnList = "slug", unique = true),
        @Index(name = "idx_problem_difficulty", columnList = "difficulty")
})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(columnDefinition = "LONGTEXT")
    private String exampleInput;

    @Column(columnDefinition = "LONGTEXT")
    private String exampleOutput;

    @Column(columnDefinition = "LONGTEXT")
    private String sampleSolution;

    @Column(unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "LONGTEXT")
    private String constraints;

    @Column(columnDefinition = "LONGTEXT")
    private String hints;

    @Column(columnDefinition = "LONGTEXT")
    private String starterCodeJava;

    @Column(columnDefinition = "LONGTEXT")
    private String starterCodeCpp;

    @Column(columnDefinition = "LONGTEXT")
    private String starterCodePython;

    @Column(columnDefinition = "LONGTEXT")
    private String visibleTestCasesJson;

    @Column(columnDefinition = "LONGTEXT")
    private String hiddenTestCasesJson;

    @Column(columnDefinition = "LONGTEXT")
    private String solutionTemplate;

    private Integer orderIndex;

    private Integer acceptanceRate;

    private Integer submissionCount;

    private Integer acceptedCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Submission> submissions = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Note> notes = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProblemTag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        acceptanceRate = 0;
        submissionCount = 0;
        acceptedCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateStats(boolean isAccepted) {
        this.submissionCount++;
        if (isAccepted) {
            this.acceptedCount++;
        }
        if (this.submissionCount > 0) {
            this.acceptanceRate = (this.acceptedCount * 100) / this.submissionCount;
        }
    }
}
