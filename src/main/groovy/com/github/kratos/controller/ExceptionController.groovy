package com.github.kratos.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kratos.http.HttpClientException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionController {

    final static Logger LOG = LoggerFactory.getLogger(ExceptionController.class)
    final ObjectMapper mapper

    @Autowired
    def ExceptionController(ObjectMapper mapper) {
        this.mapper = mapper
    }

    @ExceptionHandler(Exception.class)
    def handleException(Exception exception) {
        if (exception instanceof HttpClientException){
            final HttpClientException httpClientException = (HttpClientException)exception;
            return new ResponseEntity(mapper.writeValueAsString(httpClientException.getBody()), httpClientException.getStatus());
        } else {
            LOG.error("Unable to process request.", exception);
            return new ResponseEntity("Unable to process request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
