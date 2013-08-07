package com.github.styx.domain;

import org.springframework.http.ResponseEntity;

public class RepositoryException extends RuntimeException {

    private final ResponseEntity<String> response;

    public RepositoryException(String message) {
        super(message);
        response = null;
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
        response = null;
    }

    public RepositoryException(String message, ResponseEntity<String> response) {
        super(message);
        this.response = response;
    }

    public ResponseEntity<String> getResponse() {
        return response;
    }

}
