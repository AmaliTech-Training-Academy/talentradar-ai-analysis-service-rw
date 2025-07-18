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
public class AssessmentInputDto {

    @NotNull(message = "Scores map cannot be null")
    @Size(min = 3, message = "At least 3 skill categories must be provided.")
    private Map<
            @NotBlank(message = "Skill category cannot be blank")
            String,
            @NotNull(message = "Score cannot be null")
            @Min(value = 1, message = "Scores must be between 1 and 5.")
            @Max(value = 5, message = "Scores must be between 1 and 5.")
            Integer
            > scores;

    @Size(max = 5000, message = "Reflection cannot exceed 5000 characters")
    private String reflection;
}