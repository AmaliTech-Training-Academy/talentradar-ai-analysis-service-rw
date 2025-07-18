package com.talentradar.talentradaraianalysis.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Self-assessment is required")
    @Valid
    private AssessmentInputDto selfAssessment;

    @NotNull(message = "Manager feedback is required")
    @Valid
    private AssessmentInputDto managerFeedback;
}
