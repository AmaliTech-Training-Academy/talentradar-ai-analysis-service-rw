package com.talentradar.talentradaraianalysis.service;

import com.talentradar.talentradaraianalysis.dto.AnalysisRequest;
import com.talentradar.talentradaraianalysis.dto.AnalysisResponse;
import com.talentradar.talentradaraianalysis.dto.AnalysisComparisonRequest;
import com.talentradar.talentradaraianalysis.dto.AnalysisComparisonResponse;

public interface AIAnalysisService {


    AnalysisResponse analyzeAssessment(AnalysisRequest request);
    AnalysisComparisonResponse analyzeComparison(AnalysisComparisonRequest request);
}
