package com.talentradar.talentradaraianalysis.exception;

import com.talentradar.talentradaraianalysis.dto.AnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private AnalysisResponse createErrorResponse(HttpStatus status, String message, WebRequest request) {
        return AnalysisResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AnalysisResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", message);
        AnalysisResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<AnalysisResponse> handleApiException(ApiException ex, WebRequest request) {
        log.warn("API error: Status = {}, Message = {}", ex.getStatus(), ex.getMessage());
        AnalysisResponse errorResponse = createErrorResponse(ex.getStatus(), ex.getMessage(), request);
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AnalysisResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        AnalysisResponse errorResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while analyzing the data.", request);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
