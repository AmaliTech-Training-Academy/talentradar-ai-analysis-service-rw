package com.talentradar.talentradaraianalysis.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
