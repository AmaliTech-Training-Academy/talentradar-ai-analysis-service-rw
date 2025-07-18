package com.talentradar.talentradaraianalysis;

import com.talentradar.talentradaraianalysis.dto.*;
import com.talentradar.talentradaraianalysis.exception.ApiException;
import com.talentradar.talentradaraianalysis.model.*;
import com.talentradar.talentradaraianalysis.repository.AIAnalysisResultRepository;
import com.talentradar.talentradaraianalysis.repository.AssessmentInputRepository;
import com.talentradar.talentradaraianalysis.service.AIAnalysisServiceImpl;
import com.talentradar.talentradaraianalysis.service.MockLLMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIAnalysisServiceImplTest {
    @Mock
    private AssessmentInputRepository assessmentInputRepository;
    @Mock
    private AIAnalysisResultRepository aiAnalysisResultRepository;
    @Mock
    private MockLLMService mockLLMService;

    @InjectMocks
    private AIAnalysisServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSimpleAIAnalysis() {
        AnalysisRequest request = AnalysisRequest.builder()
                .userId("user1")
                .scores(Map.of("Technical", 4, "Communication", 5, "Growth", 3))
                .reflection("I improved my communication.")
                .build();
        when(mockLLMService.analyzeReflection(anyString())).thenReturn(new MockLLMService.ReflectionAnalysisResult("positive", List.of("communication"), List.of()));
        when(mockLLMService.generateOverallFeedback(anyList(), anyList(), anyString(), any())).thenReturn("Great job!");
        AnalysisResponse response = service.analyzeAssessment(request);
        assertEquals("user1", response.getUserId());
        assertEquals(4.0, response.getReadinessScore());
        assertTrue(response.getStrengths().contains("Technical"));
        assertTrue(response.getStrengths().contains("Communication"));
        assertTrue(response.getImprovementAreas().contains("Growth"));
        assertEquals("Great job!", response.getOverallFeedback());
    }

    @Test
    void testComparisonWithManagerAggregation() {
        AnalysisComparisonRequest request = AnalysisComparisonRequest.builder()
                .userId("user2")
                .selfScores(Map.of("Technical", 4, "Growth", 3))
                .selfReflection("Improved technical skills.")
                .managerScores(Map.of("Technical", 5, "Growth", 4))
                .managerReflection("Great technical progress.")
                .aggregate(true)
                .build();
        when(aiAnalysisResultRepository.findTopByUserIdOrderByAnalyzedAtDesc("user2")).thenReturn(Optional.empty());
        when(mockLLMService.analyzeReflection(anyString())).thenReturn(new MockLLMService.ReflectionAnalysisResult("positive", List.of("technical"), List.of()));
        when(mockLLMService.generateOverallFeedback(anyList(), anyList(), anyString(), any())).thenReturn("Aggregated feedback.");
        AnalysisComparisonResponse response = service.analyzeComparison(request);
        assertEquals("user2", response.getUserId());
        assertEquals(4.0, response.getCurrentReadinessScore());
        assertEquals("Medium Performer", response.getPerformanceLevel());
        System.out.println("Strengths: " + response.getStrengths());
        assertTrue(response.getStrengths().contains("Technical"));
        System.out.println("Improvement Areas: " + response.getImprovementAreas());
        assertEquals("Aggregated feedback.", response.getOverallFeedback());
    }

    @Test
    void testCooldownEnforced() {
        AnalysisComparisonRequest request = AnalysisComparisonRequest.builder()
                .userId("user3")
                .selfScores(Map.of("Technical", 4, "Growth", 3, "Communication", 5))
                .selfReflection("Reflection.")
                .aggregate(false)
                .build();
        AIAnalysisResult previous = AIAnalysisResult.builder()
                .userId("user3")
                .readinessScore(4.0)
                .analyzedAt(LocalDateTime.now().minusDays(10))
                .assessment(AssessmentInput.builder().skillScores(Map.of("Technical", 3, "Growth", 2, "Communication", 4)).build())
                .build();
        when(aiAnalysisResultRepository.findTopByUserIdOrderByAnalyzedAtDesc("user3")).thenReturn(Optional.of(previous));
        ApiException ex = assertThrows(ApiException.class, () -> service.analyzeComparison(request));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
    }

    @Test
    void testHistoricalImprovementSummary() {
        AnalysisComparisonRequest request = AnalysisComparisonRequest.builder()
                .userId("user4")
                .selfScores(Map.of("Technical", 5, "Growth", 4, "Communication", 4))
                .selfReflection("Reflection.")
                .aggregate(false)
                .build();
        AIAnalysisResult previous = AIAnalysisResult.builder()
                .userId("user4")
                .readinessScore(3.0)
                .analyzedAt(LocalDateTime.now().minusDays(40))
                .assessment(AssessmentInput.builder().skillScores(Map.of("Technical", 3, "Growth", 2, "Communication", 4)).build())
                .build();
        when(aiAnalysisResultRepository.findTopByUserIdOrderByAnalyzedAtDesc("user4")).thenReturn(Optional.of(previous));
        when(mockLLMService.analyzeReflection(anyString())).thenReturn(new MockLLMService.ReflectionAnalysisResult("positive", List.of(), List.of()));
        when(mockLLMService.generateOverallFeedback(anyList(), anyList(), anyString(), any())).thenReturn("Improved feedback.");
        AnalysisComparisonResponse response = service.analyzeComparison(request);
        assertEquals(3.0, response.getPreviousReadinessScore());
        assertTrue(response.getImprovementSummary().contains("improved"));
    }

    @Test
    void testPerformanceLevelClassification() {
        AnalysisComparisonRequest request = AnalysisComparisonRequest.builder()
                .userId("user5")
                .selfScores(Map.of("Technical", 3, "Growth", 3, "Communication", 3))
                .selfReflection("Reflection.")
                .aggregate(false)
                .build();
        when(aiAnalysisResultRepository.findTopByUserIdOrderByAnalyzedAtDesc("user5")).thenReturn(Optional.empty());
        when(mockLLMService.analyzeReflection(anyString())).thenReturn(new MockLLMService.ReflectionAnalysisResult("neutral", List.of(), List.of()));
        when(mockLLMService.generateOverallFeedback(anyList(), anyList(), anyString(), any())).thenReturn("Needs improvement.");
        AnalysisComparisonResponse response = service.analyzeComparison(request);
        assertEquals("Needs Improvement", response.getPerformanceLevel());
    }
} 