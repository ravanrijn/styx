package com.github.styx.controllers;

import com.github.styx.domain.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles all global exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles an exception an returns an appropriate response entity.
     *
     * @param ex  the exception to handle
     *
     * @return a response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        LOGGER.debug("Handling exception of type {} with message {}", ex.getClass(), ex.getMessage());

        RepositoryException repositoryException = findRepositoryException(ex);
        if (repositoryException != null) {
            LOGGER.debug("Found nested repository exception with response {}", repositoryException.getResponse().toString());
            return repositoryException.getResponse();
        } else {
            return new ResponseEntity("{\"code\":\"500\",\"message\":\"" + ex.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
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
