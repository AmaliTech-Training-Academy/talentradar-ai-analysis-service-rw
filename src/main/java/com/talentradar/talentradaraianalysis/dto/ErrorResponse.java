package com.talentradar.talentradaraianalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for sending a standardized error response.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;

    @NotNull(message = "Status code cannot be null")
    private int status;

    @NotBlank(message = "Error type cannot be blank")
    private String error;

    @NotBlank(message = "Error message cannot be blank")
    private String message;

    @NotBlank(message = "Request path cannot be blank")
    private String path;
}
