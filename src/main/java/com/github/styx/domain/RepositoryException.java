package com.github.styx.domain;

import org.springframework.http.ResponseEntity;

public class RepositoryException extends RuntimeException {

    private final ResponseEntity<?> response;

    public RepositoryException(String message) {
        super(message);
        response = null;
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
        response = null;
    }

    public RepositoryException(String message, ResponseEntity<?> response) {
        super(message);
        this.response = response;
    }

    public ResponseEntity<?> getResponse() {
        return response;
    }

}
