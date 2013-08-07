package com.github.styx.controllers;

import com.github.styx.domain.RepositoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleException(Exception ex) {
        RepositoryException repositoryException = findRepositoryException(ex);
        if (repositoryException != null) {
            return repositoryException.getResponse();
        } else {
            return new ResponseEntity("{\"code\":\"500\",\"message\":\"Internal server error\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private RepositoryException findRepositoryException(Throwable ex) {
        if (ex instanceof RepositoryException) {
            return (RepositoryException) ex;
        } else if (ex.getCause() != null) {
            return findRepositoryException(ex.getCause());
        }
        return null;
    }

}
