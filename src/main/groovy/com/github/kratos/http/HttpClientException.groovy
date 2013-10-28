package com.github.kratos.http

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class HttpClientException extends RuntimeException {

    final HttpHeaders headers
    final HttpStatus status
    final Map<String, Object> body

    def HttpClientException(HttpHeaders headers, HttpStatus status, Map<String, Object> body){
        this.headers = headers
        this.status = status
        this.body = body
    }

    def HttpClientException(HttpStatus status, Map<String, Object> body){
        this.status = status
        this.body = body
        headers = null
    }

}
