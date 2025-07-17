package com.talentradar.talentradaraianalysis.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "analysis_improvement_areas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisImprovementArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "improvement_area", nullable = false)
    private String improvementArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AIAnalysisResult analysisResult;
}
