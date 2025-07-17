package com.talentradar.talentradaraianalysis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ai_analysis_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private AssessmentInput assessment;

    @Column(name = "readiness_score", nullable = false)
    @NotNull(message = "Readiness score is required")
    @DecimalMin(value = "1.0", message = "Readiness score must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Readiness score cannot exceed 5.0")
    private Double readinessScore;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotEmpty(message = "At least one strength must be identified")
    private List<AnalysisStrength> strengths;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotEmpty(message = "At least one improvement area must be identified")
    private List<AnalysisImprovementArea> improvementAreas;

    @Column(name = "overall_feedback", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Overall feedback is required")
    private String overallFeedback;

    @Column(name = "analyzed_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime analyzedAt = LocalDateTime.now();
}