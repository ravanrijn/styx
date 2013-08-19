package com.github.styx.controllers;

import com.github.styx.domain.RepositoryException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    public void testHandleExceptionShouldFailWhenRepositoryExceptionCouldNotBeHandled() {
        ResponseEntity responseEntity = new ResponseEntity("not found", HttpStatus.NOT_FOUND);
        RepositoryException repositoryException = new RepositoryException("message", responseEntity);
        ResponseEntity result = globalExceptionHandler.handleException(repositoryException);
        assertEquals("Unexpected response entity", responseEntity, result);
    }

    @Test
    public void testHandleExceptionShouldFailWhenNestedRepositoryExceptionCouldNotBeHandled() {
        ResponseEntity responseEntity = new ResponseEntity("not found", HttpStatus.NOT_FOUND);
        RepositoryException repositoryException = new RepositoryException("message", responseEntity);
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException(repositoryException);
        ResponseEntity result = globalExceptionHandler.handleException(illegalArgumentException);
        assertEquals("Unexpected response entity", responseEntity, result);
    }

    @Test
    public void testHandleExceptionShouldFailWhenIllegalArgumentExceptionCouldNotBeHandled() {
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("illegal argument");
        ResponseEntity responseEntity = globalExceptionHandler.handleException(illegalArgumentException);
        assertEquals("Unexpected http status", HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Unexpected body", "{\"code\":\"500\",\"message\":\"illegal argument\"}", responseEntity.getBody());
    }

}
