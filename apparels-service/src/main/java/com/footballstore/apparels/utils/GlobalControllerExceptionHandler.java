package com.footballstore.apparels.utils;

import com.footballstore.apparels.utils.exceptions.InvalidApparelPricingException;
import com.footballstore.apparels.utils.exceptions.InvalidInputException;
import com.footballstore.apparels.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpErrorInfo handleNotFound(WebRequest request, Exception ex) {
        return createInfo(NOT_FOUND, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler({ InvalidApparelPricingException.class, InvalidInputException.class })
    public HttpErrorInfo handleUnprocessable(WebRequest request, Exception ex) {
        return createInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    private HttpErrorInfo createInfo(HttpStatus status, WebRequest request, Exception ex) {
        String path = request.getDescription(false);
        String msg  = ex.getMessage();
        log.debug("HTTP {} on {}, message={}", status, path, msg);
        return new HttpErrorInfo(status, path, msg);
    }
}
