package com.talentradar.talentradaraianalysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for sending a successful or error AI analysis response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResponse {

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "Readiness score is required")
    @DecimalMin(value = "1.0", message = "Readiness score must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Readiness score cannot exceed 5.0")
    private Double readinessScore;

    @NotEmpty(message = "Strengths list cannot be empty")
    private List<@NotBlank(message = "A strength entry cannot be blank") String> strengths;

    @NotEmpty(message = "Improvement areas list cannot be empty")
    private List<@NotBlank(message = "An improvement area entry cannot be blank") String> improvementAreas;

    @NotBlank(message = "Overall feedback cannot be blank")
    private String overallFeedback;

    // Fields for error response
    private Instant timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;

}
