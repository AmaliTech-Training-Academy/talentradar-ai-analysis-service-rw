package com.talentradar.talentradaraianalysis.repository;

import com.talentradar.talentradaraianalysis.model.AIAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIAnalysisResultRepository extends JpaRepository<AIAnalysisResult, UUID> {
    Optional<AIAnalysisResult> findTopByUserIdOrderByAnalyzedAtDesc(String userId);
}
