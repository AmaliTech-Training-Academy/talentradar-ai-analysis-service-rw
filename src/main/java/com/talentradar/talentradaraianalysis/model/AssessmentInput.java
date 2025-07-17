package com.talentradar.talentradaraianalysis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentInput {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @ElementCollection
    @CollectionTable(
            name = "assessment_skill_scores",
            joinColumns = @JoinColumn(name = "assessment_id")
    )
    @MapKeyColumn(name = "skill_category")
    @Column(name = "score")
    @NotNull(message = "Skill scores cannot be null")
    private Map<@NotBlank String, @NotNull @Min(1) @Max(5) Integer> skillScores;

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Reflection cannot exceed 2000 characters")
    private String reflection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssessmentType type = AssessmentType.SELF_ASSESSMENT;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @OneToOne(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AIAnalysisResult analysisResult;
}