package com.github.styx.controller;

import com.github.styx.domain.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.apache.commons.lang.exception.ExceptionUtils.getCause;

@ControllerAdvice
public class ExceptionController {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceError> handleException(Exception exception) {
        if (exception instanceof EndpointException) {
            final ResponseEntity<?> response = ((EndpointException) exception).getResponse();
            return new ResponseEntity<>(new ServiceError(response.getStatusCode().value(), (String) response.getBody()), response.getStatusCode());
        } else if (getCause(exception) instanceof EndpointException) {
            final ResponseEntity<?> response = ((EndpointException) exception).getResponse();
            return new ResponseEntity<>(new ServiceError(response.getStatusCode().value(), (String) response.getBody()), response.getStatusCode());
        } else {
            LOG.error("", exception);
            return new ResponseEntity<>(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to process request."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
