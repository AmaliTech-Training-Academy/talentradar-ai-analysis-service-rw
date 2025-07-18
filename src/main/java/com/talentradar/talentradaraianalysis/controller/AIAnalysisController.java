package com.talentradar.talentradaraianalysis.controller;

import com.talentradar.talentradaraianalysis.dto.AnalysisRequest;
import com.talentradar.talentradaraianalysis.dto.AnalysisResponse;
import com.talentradar.talentradaraianalysis.dto.AnalysisComparisonRequest;
import com.talentradar.talentradaraianalysis.dto.AnalysisComparisonResponse;
import com.talentradar.talentradaraianalysis.service.AIAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-analysis")
@RequiredArgsConstructor
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = aiAnalysisService.analyzeAssessment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/compare")
    public ResponseEntity<AnalysisComparisonResponse> analyzeComparison(@Valid @RequestBody AnalysisComparisonRequest request) {
        AnalysisComparisonResponse response = aiAnalysisService.analyzeComparison(request);
        return ResponseEntity.ok(response);
    }
}
