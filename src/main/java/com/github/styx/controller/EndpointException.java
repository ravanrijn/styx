package com.github.styx.controller;

import org.springframework.http.ResponseEntity;

public class EndpointException extends RuntimeException {

    private final ResponseEntity<?> response;

    public EndpointException(String message) {
        super(message);
        response = null;
    }

    public EndpointException(String message, Throwable cause) {
        super(message, cause);
        response = null;
    }

    public EndpointException(String message, ResponseEntity<?> response) {
        super(message);
        this.response = response;
    }

    public ResponseEntity<?> getResponse() {
        return response;
    }

}
