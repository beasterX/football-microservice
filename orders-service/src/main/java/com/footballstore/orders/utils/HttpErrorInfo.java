package com.footballstore.orders.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.time.ZonedDateTime;

@Getter
public class HttpErrorInfo {
    private final ZonedDateTime timestamp = ZonedDateTime.now();
    private final HttpStatus    httpStatus;
    private final String        path;
    private final String        message;

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this.httpStatus = httpStatus;
        this.path       = path;
        this.message    = message;
    }
}
