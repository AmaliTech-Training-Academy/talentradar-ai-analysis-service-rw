package com.talentradar.talentradaraianalysis.service;

import com.talentradar.talentradaraianalysis.dto.AnalysisRequest;
import com.talentradar.talentradaraianalysis.dto.AnalysisResponse;
import com.talentradar.talentradaraianalysis.dto.AnalysisComparisonRequest;
import com.talentradar.talentradaraianalysis.dto.AnalysisComparisonResponse;
import com.talentradar.talentradaraianalysis.exception.ApiException;
import com.talentradar.talentradaraianalysis.model.*;
import com.talentradar.talentradaraianalysis.repository.AIAnalysisResultRepository;
import com.talentradar.talentradaraianalysis.repository.AssessmentInputRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final AssessmentInputRepository assessmentInputRepository;
    private final AIAnalysisResultRepository aiAnalysisResultRepository;
    private final MockLLMService mockLLMService;

    @Override
    @Transactional
    public AnalysisResponse analyzeAssessment(AnalysisRequest request) {
        log.info("Starting analysis for user: {}", request.getUserId());

        // 1. Create and save the AssessmentInput entity
        AssessmentInput assessmentInput = mapToAssessmentInput(request);
        assessmentInputRepository.save(assessmentInput);
        log.debug("Saved assessment input for user: {}", request.getUserId());

        // 2. Perform the mock AI analysis
        AIAnalysisResult analysisResult = performMockAnalysis(assessmentInput);

        // 3. Save the analysis result
        aiAnalysisResultRepository.save(analysisResult);
        log.info("Successfully completed and saved analysis for user: {}", request.getUserId());

        // 4. Map to response DTO
        return mapToAnalysisResponse(analysisResult);
    }

    @Override
    @Transactional
    public AnalysisComparisonResponse analyzeComparison(AnalysisComparisonRequest request) {
        log.info("Starting comparison analysis for user: {}", request.getUserId());

        // 1. Cooldown check (30 days)
        Optional<AIAnalysisResult> previousOpt = aiAnalysisResultRepository.findTopByUserIdOrderByAnalyzedAtDesc(request.getUserId());
        if (previousOpt.isPresent()) {
            AIAnalysisResult previous = previousOpt.get();
            if (Duration.between(previous.getAnalyzedAt(), LocalDateTime.now()).toDays() < 30) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "You must wait 30 days between analyses.");
            }
        }

        // 2. Aggregate scores (if manager feedback present and aggregate=true)
        Map<String, Integer> selfScores = request.getSelfScores();
        Map<String, Integer> managerScores = request.getManagerScores();
        Map<String, Double> aggregateScores = new java.util.HashMap<>();
        if (request.isAggregate() && managerScores != null && !managerScores.isEmpty()) {
            for (String key : selfScores.keySet()) {
                int self = selfScores.getOrDefault(key, 0);
                int mgr = managerScores.getOrDefault(key, self); // fallback to self if manager missing
                aggregateScores.put(key, (self + mgr) / 2.0);
            }
            // Add manager-only categories
            for (String key : managerScores.keySet()) {
                if (!aggregateScores.containsKey(key)) {
                    aggregateScores.put(key, managerScores.get(key).doubleValue());
                }
            }
        } else {
            for (String key : selfScores.keySet()) {
                aggregateScores.put(key, selfScores.get(key).doubleValue());
            }
        }

        // 3. Calculate readiness score
        double readinessScore = aggregateScores.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 4. Strengths and improvement areas
        List<String> strengths = new ArrayList<>();
        List<String> improvementAreas = new ArrayList<>();
        aggregateScores.forEach((skill, score) -> {
            if (score >= 4) strengths.add(skill);
            if (score <= 3) improvementAreas.add(skill);
        });

        // 5. Reflection analysis (combine self and manager reflections)
        String combinedReflection = (request.getSelfReflection() == null ? "" : request.getSelfReflection()) +
                " " + (request.getManagerReflection() == null ? "" : request.getManagerReflection());
        MockLLMService.ReflectionAnalysisResult reflectionResult = mockLLMService.analyzeReflection(combinedReflection);

        // 6. Performance level
        String performanceLevel = "Needs Improvement";
        if (readinessScore >= 4.5) performanceLevel = "High Performer";
        else if (readinessScore >= 3.5) performanceLevel = "Medium Performer";

        // 7. Historical comparison/improvement summary
        Double previousScore = previousOpt.map(AIAnalysisResult::getReadinessScore).orElse(null);
        StringBuilder improvementSummary = new StringBuilder();
        if (previousOpt.isPresent()) {
            AIAnalysisResult previous = previousOpt.get();
            double diff = readinessScore - previous.getReadinessScore();
            if (diff > 0) improvementSummary.append("Your readiness score improved by ").append(String.format("%.2f", diff)).append(". ");
            else if (diff < 0) improvementSummary.append("Your readiness score decreased by ").append(String.format("%.2f", -diff)).append(". ");
            else improvementSummary.append("Your readiness score remained the same. ");
            // Skill-by-skill improvement
            Map<String, Integer> prevScores = previous.getAssessment().getSkillScores();
            for (String skill : aggregateScores.keySet()) {
                int prev = prevScores.getOrDefault(skill, 0);
                double curr = aggregateScores.get(skill);
                if (curr > prev) improvementSummary.append(skill).append(" improved from ").append(prev).append(" to ").append(String.format("%.2f", curr)).append(". ");
                else if (curr < prev) improvementSummary.append(skill).append(" decreased from ").append(prev).append(" to ").append(String.format("%.2f", curr)).append(". ");
            }
        }

        // 8. Feedback
        String overallFeedback = mockLLMService.generateOverallFeedback(strengths, improvementAreas, combinedReflection, reflectionResult);

        // 9. Build response
        return AnalysisComparisonResponse.builder()
                .userId(request.getUserId())
                .currentReadinessScore(readinessScore)
                .previousReadinessScore(previousScore)
                .strengths(strengths)
                .improvementAreas(improvementAreas)
                .performanceLevel(performanceLevel)
                .improvementSummary(improvementSummary.toString().trim())
                .overallFeedback(overallFeedback)
                .build();
    }

    private AssessmentInput mapToAssessmentInput(AnalysisRequest request) {
        return AssessmentInput.builder()
                .userId(request.getUserId())
                .skillScores(request.getScores())
                .reflection(request.getReflection())
                .type(AssessmentType.SELF_ASSESSMENT) // Assuming self-assessment for now
                .build();
    }

    private AIAnalysisResult performMockAnalysis(AssessmentInput assessmentInput) {
        Map<String, Integer> scores = assessmentInput.getSkillScores();

        // Calculate readiness score (average of all scores)
        double readinessScore = scores.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // Identify strengths (score >= 4) and improvement areas (score <= 3)
        List<String> strengthsList = new ArrayList<>();
        List<String> improvementAreasList = new ArrayList<>();
        scores.forEach((skill, score) -> {
            if (score >= 4) {
                strengthsList.add(skill);
            }
            if (score <= 3) {
                improvementAreasList.add(skill);
            }
        });

        // Use MockLLMService for reflection analysis and feedback
        MockLLMService.ReflectionAnalysisResult reflectionResult = mockLLMService.analyzeReflection(assessmentInput.getReflection());
        String overallFeedback = mockLLMService.generateOverallFeedback(strengthsList, improvementAreasList, assessmentInput.getReflection(), reflectionResult);

        // Create the result entity
        AIAnalysisResult result = AIAnalysisResult.builder()
                .userId(assessmentInput.getUserId())
                .assessment(assessmentInput)
                .readinessScore(readinessScore)
                .overallFeedback(overallFeedback)
                .build();

        // Create and associate strength entities
        List<AnalysisStrength> analysisStrengths = strengthsList.stream()
                .map(strength -> AnalysisStrength.builder().strength(strength).analysisResult(result).build())
                .collect(Collectors.toList());
        result.setStrengths(analysisStrengths);

        // Create and associate improvement area entities
        List<AnalysisImprovementArea> analysisImprovementAreas = improvementAreasList.stream()
                .map(area -> AnalysisImprovementArea.builder().improvementArea(area).analysisResult(result).build())
                .collect(Collectors.toList());
        result.setImprovementAreas(analysisImprovementAreas);

        return result;
    }

    private AnalysisResponse mapToAnalysisResponse(AIAnalysisResult result) {
        return AnalysisResponse.builder()
                .userId(result.getUserId())
                .readinessScore(result.getReadinessScore())
                .strengths(result.getStrengths().stream().map(AnalysisStrength::getStrength).collect(Collectors.toList()))
                .improvementAreas(result.getImprovementAreas().stream().map(AnalysisImprovementArea::getImprovementArea).collect(Collectors.toList()))
                .overallFeedback(result.getOverallFeedback())
                .build();
    }
}
