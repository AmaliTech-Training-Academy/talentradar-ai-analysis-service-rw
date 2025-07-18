package com.talentradar.talentradaraianalysis.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisComparisonRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Self-assessment scores cannot be null")
    @NotEmpty(message = "Self-assessment scores cannot be empty")
    @Size(min = 3, message = "At least 3 skill categories must be provided.")
    private Map<@NotBlank String, @NotNull @Min(1) @Max(5) Integer> selfScores;

    @Size(max = 5000, message = "Self reflection cannot exceed 5000 characters")
    private String selfReflection;

    // Manager feedback (optional)
    private Map<@NotBlank String, @NotNull @Min(1) @Max(5) Integer> managerScores;

    @Size(max = 5000, message = "Manager reflection cannot exceed 5000 characters")
    private String managerReflection;

    // Whether to aggregate self and manager feedback
    private boolean aggregate;
} 