package com.crm.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<ApiException> handleApiRequestException(ApiRequestException e){
        ApiException apiException = ApiException.builder()
                .body(e.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST)
                .statusCodeValue("404")
                .build();

        return new ResponseEntity<>(apiException, HttpStatus.BAD_REQUEST);
    }
}