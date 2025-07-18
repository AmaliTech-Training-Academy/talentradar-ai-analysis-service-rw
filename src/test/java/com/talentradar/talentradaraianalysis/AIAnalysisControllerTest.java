package com.talentradar.talentradaraianalysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.talentradaraianalysis.controller.AIAnalysisController;
import com.talentradar.talentradaraianalysis.dto.*;
import com.talentradar.talentradaraianalysis.exception.ApiException;
import com.talentradar.talentradaraianalysis.exception.GlobalExceptionHandler;
import com.talentradar.talentradaraianalysis.service.AIAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AIAnalysisControllerTest {
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AIAnalysisService aiAnalysisService;

    @InjectMocks
    private AIAnalysisController aiAnalysisController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiAnalysisController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return analysis response for valid request")
    void analyzeAssessment_ValidRequest_ReturnsOk() throws Exception {
        AssessmentInputDto self = AssessmentInputDto.builder()
                .scores(Map.of("Technical", 4, "Growth", 3, "Communication", 5))
                .reflection("Reflection.")
                .build();
        AssessmentInputDto manager = AssessmentInputDto.builder()
                .scores(Map.of("Technical", 5, "Growth", 4, "Communication", 5))
                .reflection("Manager feedback.")
                .build();
        AnalysisRequest req = AnalysisRequest.builder()
                .userId("user1")
                .selfAssessment(self)
                .managerFeedback(manager)
                .build();
        AnalysisResponse resp = AnalysisResponse.builder()
                .userId("user1")
                .readinessScore(4.0)
                .strengths(List.of("Technical", "Communication"))
                .improvementAreas(List.of("Growth"))
                .overallFeedback("Good job!")
                .build();
        when(aiAnalysisService.analyzeAssessment(any())).thenReturn(resp);
        mockMvc.perform(post("/api/ai-analysis/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.readinessScore").value(4.0))
                .andExpect(jsonPath("$.strengths").isArray())
                .andExpect(jsonPath("$.overallFeedback").value("Good job!"));
    }

    @Test
    @DisplayName("Should return comparison response for valid request")
    void analyzeComparison_ValidRequest_ReturnsOk() throws Exception {
        AnalysisComparisonRequest req = AnalysisComparisonRequest.builder()
                .userId("user2")
                .selfScores(Map.of("Technical", 4, "Growth", 3, "Communication", 5))
                .selfReflection("Reflection.")
                .managerScores(Map.of("Technical", 5, "Growth", 4, "Communication", 5))
                .managerReflection("Manager feedback.")
                .aggregate(true)
                .build();
        AnalysisComparisonResponse resp = AnalysisComparisonResponse.builder()
                .userId("user2")
                .currentReadinessScore(4.0)
                .previousReadinessScore(3.5)
                .strengths(List.of("Technical", "Communication"))
                .improvementAreas(List.of("Growth"))
                .performanceLevel("High Performer")
                .improvementSummary("Improved.")
                .overallFeedback("Great progress!")
                .build();
        when(aiAnalysisService.analyzeComparison(any())).thenReturn(resp);
        mockMvc.perform(post("/api/ai-analysis/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user2"))
                .andExpect(jsonPath("$.performanceLevel").value("High Performer"))
                .andExpect(jsonPath("$.overallFeedback").value("Great progress!"));
    }

    @Test
    @DisplayName("Should return 422 for cooldown error from service")
    void analyzeComparison_CooldownError_ReturnsUnprocessableEntity() throws Exception {
        AnalysisComparisonRequest req = AnalysisComparisonRequest.builder()
                .userId("user3")
                .selfScores(Map.of("Technical", 4, "Growth", 3, "Communication", 5))
                .selfReflection("Reflection.")
                .aggregate(false)
                .build();
        when(aiAnalysisService.analyzeComparison(any())).thenThrow(new ApiException("You must wait 30 days between analyses.", org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY));
        mockMvc.perform(post("/api/ai-analysis/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("You must wait 30 days between analyses."));
    }

    @Test
    @DisplayName("Should return 400 for validation error (less than 3 skills)")
    void analyzeAssessment_ValidationError_ReturnsBadRequest() throws Exception {
        AssessmentInputDto self = AssessmentInputDto.builder()
                .scores(Map.of("Technical", 4))
                .reflection("Reflection.")
                .build();
        AssessmentInputDto manager = AssessmentInputDto.builder()
                .scores(Map.of("Technical", 4))
                .reflection("Manager feedback.")
                .build();
        AnalysisRequest req = AnalysisRequest.builder()
                .userId("user4")
                .selfAssessment(self)
                .managerFeedback(manager)
                .build();
        mockMvc.perform(post("/api/ai-analysis/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
} 