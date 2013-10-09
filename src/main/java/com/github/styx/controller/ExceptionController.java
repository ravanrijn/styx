package com.github.styx.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kratos.http.HttpClientException;
import com.github.styx.domain.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.apache.commons.lang.exception.ExceptionUtils.getCause;

@ControllerAdvice
public class ExceptionController {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionController.class);
    private final ObjectMapper mapper;

    @Autowired
    public ExceptionController(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception) throws JsonProcessingException {
        if (exception instanceof EndpointException) {
            final ResponseEntity<?> response = ((EndpointException) exception).getResponse();
            if(response == null){
                return new ResponseEntity<>(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to process request."), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(new ServiceError(response.getStatusCode().value(), (String) response.getBody()), response.getStatusCode());
        } else if (getCause(exception) instanceof EndpointException) {
            final ResponseEntity<?> response = ((EndpointException) exception).getResponse();
            return new ResponseEntity<>(new ServiceError(response.getStatusCode().value(), (String) response.getBody()), response.getStatusCode());
        } else if (exception instanceof HttpClientException){
            HttpClientException httpClientException = (HttpClientException)exception;
            return new ResponseEntity<>(mapper.writeValueAsString(httpClientException.getBody()), httpClientException.getStatus());
        } else {
            LOG.error("", exception);
            return new ResponseEntity<>(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to process request."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
