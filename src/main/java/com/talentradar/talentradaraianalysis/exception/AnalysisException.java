package com.talentradar.talentradaraianalysis.exception;

/**
 * Exception thrown when there is an error during the analysis process.
 */
public class AnalysisException extends RuntimeException {
    
    public AnalysisException(String message) {
        super(message);
    }
    
    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
