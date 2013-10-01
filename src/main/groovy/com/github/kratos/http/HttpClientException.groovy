package com.github.kratos.http

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class HttpClientException extends RuntimeException {

    HttpHeaders headers
    HttpStatus status
    Map<String, Object> body

}
