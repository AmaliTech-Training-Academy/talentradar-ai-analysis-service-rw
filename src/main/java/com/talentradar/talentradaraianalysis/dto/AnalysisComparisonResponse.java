package com.talentradar.talentradaraianalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisComparisonResponse {
    private String userId;
    private Double currentReadinessScore;
    private Double previousReadinessScore;
    private List<String> strengths;
    private List<String> improvementAreas;
    private String performanceLevel;
    private String improvementSummary;
    private String overallFeedback;
} 