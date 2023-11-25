package com.crm.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApiException {
    private final String body;
    private final HttpStatus statusCode;
    private final String statusCodeValue;
}
