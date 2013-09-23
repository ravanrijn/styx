package com.github.kratos.http

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class RestClientException extends RuntimeException {

    HttpHeaders headers
    HttpStatus status
    Map<String, Object> body

}
