package com.crm.backend.others;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;



@AllArgsConstructor
@Getter
@Setter
public class ApiResponse {
    private final String body;
    private final HttpStatus status;
    private final Integer statusCode;

    public ApiResponse(String body) {
        this.body = body;
        this.status = HttpStatus.OK;
        this.statusCode = HttpStatusCode.valueOf(HttpStatus.OK.value()).value();
    }
}
